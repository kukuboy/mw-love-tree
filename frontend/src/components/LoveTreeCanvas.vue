<template>
  <div ref="containerRef" class="canvas-wrapper">
    <canvas ref="canvasRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  stage: string
  leafCount: number
  flowerCount: number
}>()

const containerRef = ref<HTMLDivElement>()
const canvasRef = ref<HTMLCanvasElement>()

// Canvas state
let ctx: CanvasRenderingContext2D | null = null
let animId = 0
let W = 0
let H = 0
let dpr = 1
let time = 0
let globalPtIdx = 0

// ---- Types ----
interface TermPt {
  x: number
  y: number
  angle: number
  idx: number
}

interface Sparkle {
  x: number
  y: number
  size: number
  speed: number
  phase: number
}

interface Petal {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  rotation: number
  rotSpeed: number
  opacity: number
  hue: number
}

let termPoints: TermPt[] = []
let sparkles: Sparkle[] = []
let petals: Petal[] = []

// ---- Seeded deterministic random for consistent hand-drawn look ----
function srand(seed: number): number {
  const x = Math.sin(seed + 1) * 10000
  return x - Math.floor(x)
}

// ---- Stage configuration ----
function stageCfg() {
  switch (props.stage) {
    case 'SEED':
      return { trunkH: 0, trunkW: 0, branchD: 0, mainB: 0, branchLen: 0, maxLeaves: 0, maxFlowers: 0, hasFruit: false }
    case 'SPROUT':
      return { trunkH: 30, trunkW: 3, branchD: 0, mainB: 0, branchLen: 0, maxLeaves: 2, maxFlowers: 0, hasFruit: false }
    case 'SAPLING':
      return { trunkH: 60, trunkW: 7, branchD: 2, mainB: 3, branchLen: 35, maxLeaves: 10, maxFlowers: 0, hasFruit: false }
    case 'BLOOM':
      return { trunkH: 85, trunkW: 10, branchD: 3, mainB: 4, branchLen: 40, maxLeaves: 30, maxFlowers: 15, hasFruit: false }
    case 'FRUIT':
      return { trunkH: 100, trunkW: 12, branchD: 3, mainB: 5, branchLen: 45, maxLeaves: 50, maxFlowers: 20, hasFruit: true }
    default:
      return { trunkH: 0, trunkW: 0, branchD: 0, mainB: 0, branchLen: 0, maxLeaves: 0, maxFlowers: 0, hasFruit: false }
  }
}

// =============================================================
// DRAWING FUNCTIONS
// =============================================================

/** Dirt mound at tree base */
function drawGround(cx: number, by: number) {
  ctx.beginPath()
  ctx.ellipse(cx, by, 30, 10, 0, 0, Math.PI * 2)
  ctx.fillStyle = '#C4A882'
  ctx.fill()

  // Rough texture strokes
  for (let i = 0; i < 4; i++) {
    ctx.beginPath()
    ctx.ellipse(
      cx + (srand(i * 50) - 0.5) * 16,
      by + (srand(i * 100) - 0.5) * 6,
      18 + srand(i * 150) * 10,
      5 + srand(i * 200) * 4,
      0, 0, Math.PI * 2,
    )
    ctx.strokeStyle = `rgba(160, 130, 90, ${0.25 + srand(i * 250) * 0.3})`
    ctx.lineWidth = 1.5
    ctx.stroke()
  }
}

/** SEED: single seed buried in dirt */
function drawSeedShape(cx: number, by: number) {
  const sy = by - 12
  ctx.beginPath()
  ctx.ellipse(cx, sy, 5, 7, 0.1, 0, Math.PI * 2)
  ctx.fillStyle = '#6B4C3B'
  ctx.fill()
  // Subtle highlight
  ctx.beginPath()
  ctx.ellipse(cx - 1.5, sy - 3, 2, 3, 0.2, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(255,255,255,0.12)'
  ctx.fill()
}

/** SPROUT: thin green stem with two tiny leaves */
function drawSproutStem(cx: number, by: number) {
  const topY = by - 28
  ctx.beginPath()
  ctx.moveTo(cx, by)
  ctx.quadraticCurveTo(
    cx + Math.sin(time * 0.001) * 3,
    by - 14,
    cx + Math.sin(time * 0.001) * 2,
    topY,
  )
  ctx.strokeStyle = '#6B9B6B'
  ctx.lineWidth = 3
  ctx.lineCap = 'round'
  ctx.stroke()

  // Two tiny leaves
  drawLeafAt(cx + Math.sin(time * 0.001) * 2, topY, -0.5, 7)
  drawLeafAt(cx + Math.sin(time * 0.001) * 2, topY, 0.5, 7)
}

/** Trunk: warm brown #8B6F5C, overlapping rough bezier strokes */
function drawTrunk(cx: number, by: number, trunkH: number, trunkW: number) {
  const sway = Math.sin(time * 0.0006) * 2
  const endX = cx + sway
  const endY = by - trunkH

  for (let i = 0; i < 5; i++) {
    const ox = (srand(i * 100) - 0.5) * 3
    const oy = (srand(i * 200) - 0.5) * 2

    ctx.beginPath()
    ctx.moveTo(cx - trunkW * 0.2 + ox, by + oy)
    const cp1x = cx + sway * 0.3 + (srand(10 + i * 7) - 0.5) * 5
    const cp1y = by - trunkH * 0.35
    const cp2x = endX + (srand(20 + i * 7) - 0.5) * 4
    const cp2y = by - trunkH * 0.7
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, endX + ox, endY + oy)

    ctx.strokeStyle = i < 2
      ? '#8B6F5C'
      : `rgba(139, 111, 92, ${0.4 + srand(i * 300) * 0.3})`
    ctx.lineWidth = trunkW * (0.7 + srand(i * 350) * 0.3) + srand(i * 400) * 1.5
    ctx.lineCap = 'round'
    ctx.stroke()
  }

  return { x: endX, y: endY }
}

/** Recursive branch drawing with hand-drawn overlapping strokes */
function drawBranches(
  cx: number, cy: number,
  len: number,
  angle: number,
  depth: number,
  maxD: number,
  baseSeed: number,
) {
  const sway = Math.sin(time * 0.001 + baseSeed * 0.1) * 0.04 * (maxD - depth + 1)
  const endX = cx + Math.cos(angle + sway) * len
  const endY = cy + Math.sin(angle + sway) * len

  // 3 overlapping strokes for hand-drawn feel
  for (let i = 0; i < 3; i++) {
    const ox = (srand(baseSeed + i * 100) - 0.5) * 2
    const oy = (srand(baseSeed + i * 200) - 0.5) * 2

    ctx.beginPath()
    ctx.moveTo(cx + ox, cy + oy)
    const cpx = (cx + endX) / 2 + (srand(baseSeed + 300) - 0.5) * len * 0.25
    const cpy = (cy + endY) / 2 + (srand(baseSeed + 400) - 0.5) * len * 0.25 + sway * 6
    ctx.quadraticCurveTo(cpx + ox * 0.5, cpy + oy * 0.5, endX + ox, endY + oy)

    const bw = Math.max(1.2, 2.8 - depth * 0.65)
    ctx.strokeStyle = depth < 2 ? '#8B6F5C' : '#A0806C'
    ctx.lineWidth = bw
    ctx.lineCap = 'round'
    ctx.stroke()
  }

  // Recurse children
  if (depth < maxD) {
    const numChildren = 2 + Math.floor(srand(baseSeed + 500) * 1.2)
    for (let i = 0; i < numChildren; i++) {
      const childAngle = angle + (srand(baseSeed + i * 50 + 600) - 0.5) * Math.PI * 0.3
      const childLen = len * (0.5 + srand(baseSeed + i * 70 + 700) * 0.15)
      drawBranches(endX, endY, childLen, childAngle, depth + 1, maxD, baseSeed + i * 137 + 1000)
    }
  } else {
    // Record terminal point
    termPoints.push({ x: endX, y: endY, angle: angle + sway, idx: globalPtIdx++ })
  }
}

/** Green leaf: ellipse with pointed tip, slight color variations */
function drawLeafAt(x: number, y: number, angle: number, size: number) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate(angle + Math.sin(time * 0.002 + x * 0.01) * 0.04)

  const greenVar = Math.floor(srand(x * 100 + y) * 20 - 10)
  ctx.fillStyle = `hsl(${110 + greenVar}, 38%, ${55 + greenVar * 0.5}%)`
  ctx.strokeStyle = `hsl(${100 + greenVar}, 30%, 40%)`
  ctx.lineWidth = 0.8

  // Ellipse body with pointed tip
  ctx.beginPath()
  ctx.ellipse(0, -size * 0.45, size * 0.28, size * 0.45, 0, 0, Math.PI * 2)
  ctx.lineTo(0, -size)
  ctx.closePath()
  ctx.fill()
  ctx.stroke()

  // Centre vein
  ctx.beginPath()
  ctx.moveTo(0, -size * 0.1)
  ctx.lineTo(0, -size * 0.65)
  ctx.strokeStyle = 'rgba(50,100,50,0.18)'
  ctx.lineWidth = 0.5
  ctx.stroke()

  ctx.restore()
}

/** 5-petal flower with centre circle */
function drawFlowerAt(x: number, y: number, angle: number, size: number, color: string) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate(angle + Math.sin(time * 0.0015 + x * 0.01) * 0.03)

  // 5 petals arranged in a circle
  for (let i = 0; i < 5; i++) {
    const a = (i / 5) * Math.PI * 2
    const px = Math.cos(a) * size * 0.5
    const py = Math.sin(a) * size * 0.5
    ctx.beginPath()
    ctx.ellipse(px, py, size * 0.32, size * 0.28, a, 0, Math.PI * 2)
    ctx.fillStyle = color
    ctx.fill()
    // Subtle petal highlight
    ctx.beginPath()
    ctx.ellipse(px - 0.5, py - 0.5, size * 0.1, size * 0.1, a, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255,255,255,0.2)'
    ctx.fill()
  }

  // Yellow centre
  ctx.beginPath()
  ctx.arc(0, 0, size * 0.15, 0, Math.PI * 2)
  ctx.fillStyle = '#FFE44D'
  ctx.fill()

  ctx.restore()
}

/** Small golden fruit with glow */
function drawFruitAt(x: number, y: number) {
  ctx.save()
  ctx.translate(x, y)

  // Outer glow
  ctx.beginPath()
  ctx.arc(0, 0, 7, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(212, 165, 116, 0.12)'
  ctx.fill()

  // Fruit body
  ctx.beginPath()
  ctx.arc(0, 0, 4.5, 0, Math.PI * 2)
  ctx.fillStyle = '#D4A574'
  ctx.fill()

  // Highlight
  ctx.beginPath()
  ctx.arc(-1.2, -1.2, 1.5, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(255,255,255,0.35)'
  ctx.fill()

  // Stem nub
  ctx.beginPath()
  ctx.arc(0, -4.5, 1, 0, Math.PI * 2)
  ctx.fillStyle = '#8B7355'
  ctx.fill()

  ctx.restore()
}

// =============================================================
// PARTICLE SYSTEMS
// =============================================================

function initSparkles() {
  sparkles = []
  for (let i = 0; i < 25; i++) {
    sparkles.push({
      x: Math.random() * W,
      y: Math.random() * H * 0.7,
      size: 1 + Math.random() * 2,
      speed: 0.3 + Math.random() * 0.8,
      phase: Math.random() * Math.PI * 2,
    })
  }
}

function initPetals() {
  petals = []
  for (let i = 0; i < 12; i++) {
    petals.push(createPetal())
  }
}

function createPetal(): Petal {
  return {
    x: -20 + Math.random() * (W + 40),
    y: -20 - Math.random() * 80,
    vx: -0.4 + Math.random() * 0.8,
    vy: 0.3 + Math.random() * 0.4,
    size: 4 + Math.random() * 5,
    rotation: Math.random() * Math.PI * 2,
    rotSpeed: (Math.random() - 0.5) * 0.04,
    opacity: 0.3 + Math.random() * 0.4,
    hue: 330 + Math.floor(Math.random() * 30),
  }
}

/** Twinkling 4-point star sparkles */
function drawSparkles() {
  for (const s of sparkles) {
    const twinkle = Math.sin(time * 0.002 * s.speed + s.phase)
    const alpha = Math.max(0.08, (twinkle * 0.5 + 0.5)) * 0.7

    ctx.beginPath()
    const r = s.size
    for (let i = 0; i < 4; i++) {
      const a = (i / 4) * Math.PI * 2 - Math.PI / 2
      ctx.lineTo(s.x + Math.cos(a) * r, s.y + Math.sin(a) * r)
      const a2 = a + Math.PI / 4
      ctx.lineTo(s.x + Math.cos(a2) * r * 0.3, s.y + Math.sin(a2) * r * 0.3)
    }
    ctx.closePath()
    ctx.fillStyle = `rgba(255, 220, 180, ${alpha})`
    ctx.fill()
  }
}

/** Falling petal particles for BLOOM / FRUIT stages */
function updateAndDrawPetals() {
  for (const p of petals) {
    p.x += p.vx + Math.sin(time * 0.001 + p.y * 0.01) * 0.2
    p.y += p.vy
    p.rotation += p.rotSpeed
    p.vy = Math.min(p.vy + 0.008, 1)

    ctx.save()
    ctx.translate(p.x, p.y)
    ctx.rotate(p.rotation)
    ctx.beginPath()
    ctx.ellipse(0, 0, p.size, p.size * 0.4, 0, 0, Math.PI * 2)
    ctx.fillStyle = `hsla(${p.hue}, 70%, 75%, ${p.opacity})`
    ctx.fill()
    ctx.restore()

    // Recycle when off screen
    if (p.y > H + 20 || p.x < -30 || p.x > W + 30) {
      const fresh = createPetal()
      fresh.y = -10
      fresh.x = Math.random() * W
      Object.assign(p, fresh)
    }
  }
}

// =============================================================
// MAIN RENDER LOOP
// =============================================================

function render(ts: number) {
  if (!ctx) return

  time = ts

  const stg = stageCfg()
  const cx = W / 2
  const by = H - 22

  // Clear
  ctx.clearRect(0, 0, W, H)

  // Warm romantic background glow
  const grad = ctx.createRadialGradient(cx, by - 50, 0, cx, by - 50, Math.max(W, H) * 0.6)
  grad.addColorStop(0, 'rgba(255, 230, 240, 0.25)')
  grad.addColorStop(0.5, 'rgba(255, 245, 248, 0.1)')
  grad.addColorStop(1, 'rgba(255, 255, 255, 0)')
  ctx.fillStyle = grad
  ctx.fillRect(0, 0, W, H)

  // Ground
  drawGround(cx, by)

  // Stage-specific drawing
  if (props.stage === 'SEED') {
    drawSeedShape(cx, by)
  } else if (props.stage === 'SPROUT') {
    drawSproutStem(cx, by)
  } else {
    // Draw trunk
    const top = drawTrunk(cx, by, stg.trunkH, stg.trunkW)

    // Draw branches
    globalPtIdx = 0
    termPoints = []

    if (stg.branchD > 0) {
      const spread = Math.PI * 0.5
      for (let i = 0; i < stg.mainB; i++) {
        const angle = -Math.PI / 2 - spread / 2 + (spread / Math.max(1, stg.mainB - 1)) * i
        drawBranches(top.x, top.y, stg.branchLen, angle, 0, stg.branchD, 5000 + i * 1000)
      }
    }

    // ---- Place leaves, flowers, and fruits at terminal points ----
    if (termPoints.length > 0) {
      // Deterministic shuffle so leaves don't jump between frames
      const sorted = [...termPoints].sort((a, b) => srand(a.idx * 777) - srand(b.idx * 777))

      const leavesToDraw = Math.min(stg.maxLeaves, props.leafCount, sorted.length)
      for (let i = 0; i < leavesToDraw; i++) {
        const pt = sorted[i]
        drawLeafAt(
          pt.x, pt.y,
          pt.angle - Math.PI / 2 + (srand(pt.idx * 333) - 0.5) * 0.6,
          7 + srand(pt.idx * 444) * 5,
        )
      }

      // Draw flowers (on unused terminal points)
      if (stg.maxFlowers > 0 && props.flowerCount > 0) {
        const flowerStart = leavesToDraw
        const flowerColors = ['#FF9CB5', '#FF7B9C', '#E87D9E', '#D4627A', '#FFA8C3', '#F08AAC', '#FF8FA3']
        const flowersToDraw = Math.min(stg.maxFlowers, props.flowerCount, sorted.length - flowerStart)
        for (let i = 0; i < flowersToDraw; i++) {
          const pt = sorted[flowerStart + i]
          const cIdx = Math.floor(srand(pt.idx * 555) * flowerColors.length)
          drawFlowerAt(
            pt.x, pt.y,
            pt.angle - Math.PI / 2 + (srand(pt.idx * 666) - 0.5) * 0.4,
            5 + srand(pt.idx * 777) * 3,
            flowerColors[cIdx],
          )
        }
      }

      // Draw fruits for FRUIT stage
      if (stg.hasFruit) {
        const fruitStart = leavesToDraw + Math.min(stg.maxFlowers, props.flowerCount)
        const fruitsToDraw = Math.min(6, sorted.length - fruitStart)
        for (let i = 0; i < fruitsToDraw; i++) {
          const pt = sorted[fruitStart + i]
          drawFruitAt(pt.x, pt.y)
        }
      }
    }
  }

  // Particles on every frame
  drawSparkles()

  if (props.stage === 'BLOOM' || props.stage === 'FRUIT') {
    updateAndDrawPetals()
  }

  animId = requestAnimationFrame(render)
}

// =============================================================
// LIFECYCLE
// =============================================================

function setupCanvas() {
  const container = containerRef.value
  const canvas = canvasRef.value
  if (!container || !canvas) return

  const rect = container.getBoundingClientRect()
  dpr = window.devicePixelRatio || 1
  W = rect.width
  H = rect.height

  canvas.width = W * dpr
  canvas.height = H * dpr
  canvas.style.width = `${W}px`
  canvas.style.height = `${H}px`

  ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.scale(dpr, dpr)

  initSparkles()
  initPetals()

  animId = requestAnimationFrame(render)
}

function onResize() {
  // Cancel current loop
  if (animId) cancelAnimationFrame(animId)

  // Re-init with new dimensions
  const container = containerRef.value
  const canvas = canvasRef.value
  if (!container || !canvas) return

  const rect = container.getBoundingClientRect()
  dpr = window.devicePixelRatio || 1
  W = rect.width
  H = rect.height

  canvas.width = W * dpr
  canvas.height = H * dpr
  canvas.style.width = `${W}px`
  canvas.style.height = `${H}px`

  ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.scale(dpr, dpr)

  initSparkles()
  initPetals()

  animId = requestAnimationFrame(render)
}

onMounted(() => {
  setupCanvas()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (animId) cancelAnimationFrame(animId)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.canvas-wrapper {
  width: 100%;
  height: 100%;
  min-height: 400px;
  max-height: 500px;
  position: relative;
  overflow: hidden;
  border-radius: var(--border-radius);
}

canvas {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
