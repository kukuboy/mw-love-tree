<template>
  <div class="canvas-root">
    <div ref="containerRef" class="canvas-wrapper" :class="{ 'is-fullscreen': isFullscreen }">
      <canvas
        ref="canvasRef"
        :class="{ 'has-hover': hoveredLeafIdx >= 0 }"
        @click="handleCanvasClick"
        @mousemove="handleCanvasMove"
        @mouseleave="handleCanvasLeave"
        @wheel.prevent="handleWheel"
        @mousedown="handlePanStart"
        @touchstart.passive="handleTouchStart"
        @touchmove.passive="handleTouchMove"
        @touchend.passive="handleTouchEnd"
      />

      <!-- Hover tooltip -->
      <div
        v-if="hoverTooltip.visible"
        class="leaf-tooltip"
        :style="{ left: hoverTooltip.x + 'px', top: hoverTooltip.y + 'px' }"
      >
        {{ hoverTooltip.text }}
      </div>

      <!-- Controls overlay -->
      <div v-if="!isFullscreen" class="canvas-controls">
        <button class="ctrl-btn" title="全屏查看" @click="enterFullscreen">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5"/>
          </svg>
        </button>
      </div>

      <!-- Fullscreen controls -->
      <div v-if="isFullscreen" class="fs-controls">
        <div class="fs-zoom-info">缩放: {{ Math.round(zoom * 100) }}%</div>
        <div class="fs-btns">
          <button class="ctrl-btn" title="放大" @click="zoomBy(1.2)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35M11 8v6M8 11h6"/>
            </svg>
          </button>
          <button class="ctrl-btn" title="缩小" @click="zoomBy(1 / 1.2)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35M8 11h6"/>
            </svg>
          </button>
          <button class="ctrl-btn" title="重置" @click="resetView">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5"/>
            </svg>
          </button>
          <button class="ctrl-btn" title="关闭" @click="exitFullscreen">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

export interface TreeEvent {
  id: number
  title: string
  images?: string[]
  eventType?: string
}

const props = defineProps<{
  stage: string
  leafCount: number
  flowerCount: number
  events?: TreeEvent[]
}>()

const emit = defineEmits<{
  (e: 'leaf-click', eventId: number): void
  (e: 'fullscreen-change', isFullscreen: boolean): void
}>()

const containerRef = ref<HTMLDivElement>()
const canvasRef = ref<HTMLCanvasElement>()

// Canvas state
let ctx!: CanvasRenderingContext2D
let animId = 0
let W = 0
let H = 0
let dpr = 1
let time = 0
let globalPtIdx = 0

// View state (for fullscreen zoom/pan)
const isFullscreen = ref(false)
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)

// Hover state
const hoveredLeafIdx = ref(-1)
const hoverTooltip = ref({ visible: false, x: 0, y: 0, text: '' })

// Pan state
let isPanning = false
let panStartX = 0
let panStartY = 0
let panOriginX = 0
let panOriginY = 0
let panMoved = false

// Pinch state
let pinchStartDist = 0
let pinchStartZoom = 1

// Image cache: url -> HTMLImageElement
const imageCache = new Map<string, HTMLImageElement>()

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

interface LeafHit {
  x: number
  y: number
  size: number
  eventId: number
  title: string
}

let termPoints: TermPt[] = []
let leafHits: LeafHit[] = []
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
      return { trunkH: 60, trunkW: 7, branchD: 2, mainB: 3, branchLen: 35, maxLeaves: 30, maxFlowers: 0, hasFruit: false }
    case 'BLOOM':
      return { trunkH: 85, trunkW: 10, branchD: 3, mainB: 4, branchLen: 40, maxLeaves: 50, maxFlowers: 15, hasFruit: false }
    case 'FRUIT':
      return { trunkH: 100, trunkW: 12, branchD: 3, mainB: 5, branchLen: 45, maxLeaves: 80, maxFlowers: 20, hasFruit: true }
    default:
      return { trunkH: 0, trunkW: 0, branchD: 0, mainB: 0, branchLen: 0, maxLeaves: 0, maxFlowers: 0, hasFruit: false }
  }
}

// =============================================================
// IMAGE LOADING
// =============================================================

function getImage(url: string): HTMLImageElement | null {
  if (!url) return null
  const cached = imageCache.get(url)
  if (cached) return cached
  const img = new Image()
  img.src = url
  img.onload = () => {
    // Trigger redraw on next frame
  }
  imageCache.set(url, img)
  return img
}

// =============================================================
// DRAWING FUNCTIONS
// =============================================================

function drawGround(cx: number, by: number) {
  ctx.beginPath()
  ctx.ellipse(cx, by, 30, 10, 0, 0, Math.PI * 2)
  ctx.fillStyle = '#C4A882'
  ctx.fill()

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

function drawSeedShape(cx: number, by: number) {
  const sy = by - 12
  ctx.beginPath()
  ctx.ellipse(cx, sy, 5, 7, 0.1, 0, Math.PI * 2)
  ctx.fillStyle = '#6B4C3B'
  ctx.fill()
  ctx.beginPath()
  ctx.ellipse(cx - 1.5, sy - 3, 2, 3, 0.2, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(255,255,255,0.12)'
  ctx.fill()
}

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

  // Two tiny leaves (only if we have events for them, otherwise decorative)
  drawDecorativeLeaf(cx + Math.sin(time * 0.001) * 2, topY, -0.5, 7)
  drawDecorativeLeaf(cx + Math.sin(time * 0.001) * 2, topY, 0.5, 7)
}

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

  if (depth < maxD) {
    const numChildren = 2 + Math.floor(srand(baseSeed + 500) * 1.2)
    for (let i = 0; i < numChildren; i++) {
      const childAngle = angle + (srand(baseSeed + i * 50 + 600) - 0.5) * Math.PI * 0.3
      const childLen = len * (0.5 + srand(baseSeed + i * 70 + 700) * 0.15)
      drawBranches(endX, endY, childLen, childAngle, depth + 1, maxD, baseSeed + i * 137 + 1000)
    }
  } else {
    termPoints.push({ x: endX, y: endY, angle: angle + sway, idx: globalPtIdx++ })
  }
}

/** Decorative green leaf (no event attached) */
function drawDecorativeLeaf(x: number, y: number, angle: number, size: number) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate(angle + Math.sin(time * 0.002 + x * 0.01) * 0.04)

  const greenVar = Math.floor(srand(x * 100 + y) * 20 - 10)
  ctx.fillStyle = `hsl(${110 + greenVar}, 38%, ${55 + greenVar * 0.5}%)`
  ctx.strokeStyle = `hsl(${100 + greenVar}, 30%, 40%)`
  ctx.lineWidth = 0.8

  ctx.beginPath()
  ctx.ellipse(0, -size * 0.45, size * 0.28, size * 0.45, 0, 0, Math.PI * 2)
  ctx.lineTo(0, -size)
  ctx.closePath()
  ctx.fill()
  ctx.stroke()

  ctx.beginPath()
  ctx.moveTo(0, -size * 0.1)
  ctx.lineTo(0, -size * 0.65)
  ctx.strokeStyle = 'rgba(50,100,50,0.18)'
  ctx.lineWidth = 0.5
  ctx.stroke()

  ctx.restore()
}

/**
 * Draw an event leaf: a rounded-rect leaf shape with either an image thumbnail
 * or the event title text. Records a hit area for click detection.
 */
function drawEventLeaf(pt: TermPt, event: TreeEvent, leafIdx: number, size: number) {
  const isHovered = hoveredLeafIdx.value === leafIdx
  const drawSize = isHovered ? size * 1.18 : size

  ctx.save()
  ctx.translate(pt.x, pt.y)
  const angle = pt.angle - Math.PI / 2 + (srand(pt.idx * 333) - 0.5) * 0.6
  ctx.rotate(angle + Math.sin(time * 0.002 + pt.x * 0.01) * 0.04)

  const halfW = drawSize * 0.7
  const halfH = drawSize * 0.9

  // Leaf body: rounded rectangle
  const radius = drawSize * 0.35
  ctx.beginPath()
  roundRectPath(ctx, -halfW, -halfH, halfW * 2, halfH * 2, radius)

  // Leaf background gradient (green)
  const greenVar = Math.floor(srand(pt.idx * 100) * 20 - 10)
  const grad = ctx.createLinearGradient(0, -halfH, 0, halfH)
  grad.addColorStop(0, `hsl(${110 + greenVar}, 45%, ${60 + greenVar * 0.5}%)`)
  grad.addColorStop(1, `hsl(${100 + greenVar}, 40%, ${45 + greenVar * 0.5}%)`)
  ctx.fillStyle = grad
  ctx.fill()

  // Leaf border
  ctx.strokeStyle = isHovered
    ? `hsl(45, 80%, 55%)`
    : `hsl(${100 + greenVar}, 35%, 35%)`
  ctx.lineWidth = isHovered ? 2.2 : 1
  ctx.stroke()

  // Tip of the leaf (small triangle pointing outward)
  ctx.beginPath()
  ctx.moveTo(0, -halfH)
  ctx.lineTo(-drawSize * 0.12, -halfH - drawSize * 0.18)
  ctx.lineTo(drawSize * 0.12, -halfH - drawSize * 0.18)
  ctx.closePath()
  ctx.fillStyle = `hsl(${100 + greenVar}, 40%, ${45 + greenVar * 0.5}%)`
  ctx.fill()

  // Centre vein
  ctx.beginPath()
  ctx.moveTo(0, halfH * 0.8)
  ctx.lineTo(0, -halfH * 0.85)
  ctx.strokeStyle = 'rgba(50,100,50,0.25)'
  ctx.lineWidth = 0.6
  ctx.stroke()

  // Content: image or title
  const eventImage = event.images && event.images.length > 0 ? event.images[0] : null

  if (eventImage) {
    const img = getImage(eventImage)
    // Inner rounded clip area for the image
    ctx.save()
    ctx.beginPath()
    const innerPad = drawSize * 0.18
    roundRectPath(ctx, -halfW + innerPad, -halfH + innerPad, (halfW - innerPad) * 2, (halfH - innerPad) * 2, radius * 0.7)
    ctx.clip()

    if (img && img.complete && img.naturalWidth > 0) {
      // Cover-fit the image into the inner area
      const innerW = (halfW - innerPad) * 2
      const innerH = (halfH - innerPad) * 2
      const imgRatio = img.naturalWidth / img.naturalHeight
      const boxRatio = innerW / innerH
      let dw = innerW, dh = innerH
      let dx = -halfW + innerPad, dy = -halfH + innerPad
      if (imgRatio > boxRatio) {
        dw = innerH * imgRatio
        dx = -dw / 2
      } else {
        dh = innerW / imgRatio
        dy = -dh / 2
      }
      ctx.drawImage(img, dx, dy, dw, dh)
    } else {
      // Placeholder while image is loading
      ctx.fillStyle = 'rgba(255,255,255,0.25)'
      ctx.fillRect(-halfW + innerPad, -halfH + innerPad, (halfW - innerPad) * 2, (halfH - innerPad) * 2)
      // Small camera emoji placeholder
      ctx.fillStyle = 'rgba(255,255,255,0.6)'
      ctx.font = `${drawSize * 0.6}px sans-serif`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('📷', 0, 0)
    }
    ctx.restore()
  } else {
    // Draw title text on the leaf
    ctx.fillStyle = 'rgba(255, 255, 255, 0.95)'
    ctx.font = `600 ${drawSize * 0.32}px "Noto Serif SC", serif`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'

    // Word wrap title into up to 3 lines
    const maxW = halfW * 1.7
    const lines = wrapText(event.title, maxW, ctx)
    const lineHeight = drawSize * 0.38
    const startLineY = -((lines.length - 1) * lineHeight) / 2
    const visibleLines = lines.slice(0, 3)
    if (lines.length > 3) {
      visibleLines[2] = visibleLines[2].slice(0, -1) + '…'
    }
    visibleLines.forEach((line, i) => {
      ctx.fillText(line, 0, startLineY + i * lineHeight)
    })
  }

  // Hover glow
  if (isHovered) {
    ctx.shadowColor = 'rgba(255, 215, 130, 0.9)'
    ctx.shadowBlur = 12
    ctx.beginPath()
    roundRectPath(ctx, -halfW, -halfH, halfW * 2, halfH * 2, radius)
    ctx.strokeStyle = 'rgba(255, 215, 130, 0.9)'
    ctx.lineWidth = 1.5
    ctx.stroke()
  }

  ctx.restore()

  // Record hit area in canvas coords (approximate as circle for hit testing)
  leafHits.push({
    x: pt.x,
    y: pt.y,
    size: Math.max(halfW, halfH) * 1.05,
    eventId: event.id,
    title: event.title,
  })
}

function roundRectPath(c: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number) {
  const rr = Math.min(r, w / 2, h / 2)
  c.moveTo(x + rr, y)
  c.lineTo(x + w - rr, y)
  c.quadraticCurveTo(x + w, y, x + w, y + rr)
  c.lineTo(x + w, y + h - rr)
  c.quadraticCurveTo(x + w, y + h, x + w - rr, y + h)
  c.lineTo(x + rr, y + h)
  c.quadraticCurveTo(x, y + h, x, y + h - rr)
  c.lineTo(x, y + rr)
  c.quadraticCurveTo(x, y, x + rr, y)
  c.closePath()
}

function wrapText(text: string, maxWidth: number, c: CanvasRenderingContext2D): string[] {
  const lines: string[] = []
  let current = ''
  for (const ch of text) {
    const test = current + ch
    if (c.measureText(test).width > maxWidth && current.length > 0) {
      lines.push(current)
      current = ch
    } else {
      current = test
    }
  }
  if (current) lines.push(current)
  return lines
}

/** 5-petal flower with centre circle */
function drawFlowerAt(x: number, y: number, angle: number, size: number, color: string) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate(angle + Math.sin(time * 0.0015 + x * 0.01) * 0.03)

  for (let i = 0; i < 5; i++) {
    const a = (i / 5) * Math.PI * 2
    const px = Math.cos(a) * size * 0.5
    const py = Math.sin(a) * size * 0.5
    ctx.beginPath()
    ctx.ellipse(px, py, size * 0.32, size * 0.28, a, 0, Math.PI * 2)
    ctx.fillStyle = color
    ctx.fill()
    ctx.beginPath()
    ctx.ellipse(px - 0.5, py - 0.5, size * 0.1, size * 0.1, a, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255,255,255,0.2)'
    ctx.fill()
  }

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

  ctx.beginPath()
  ctx.arc(0, 0, 7, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(212, 165, 116, 0.12)'
  ctx.fill()

  ctx.beginPath()
  ctx.arc(0, 0, 4.5, 0, Math.PI * 2)
  ctx.fillStyle = '#D4A574'
  ctx.fill()

  ctx.beginPath()
  ctx.arc(-1.2, -1.2, 1.5, 0, Math.PI * 2)
  ctx.fillStyle = 'rgba(255,255,255,0.35)'
  ctx.fill()

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

  ctx.clearRect(0, 0, W, H)

  // Apply zoom/pan transform for fullscreen mode
  ctx.save()
  if (isFullscreen.value && (zoom.value !== 1 || panX.value !== 0 || panY.value !== 0)) {
    ctx.translate(panX.value, panY.value)
    ctx.translate(cx, by - 50)
    ctx.scale(zoom.value, zoom.value)
    ctx.translate(-cx, -(by - 50))
  }

  // Warm romantic background glow
  const grad = ctx.createRadialGradient(cx, by - 50, 0, cx, by - 50, Math.max(W, H) * 0.6)
  grad.addColorStop(0, 'rgba(255, 230, 240, 0.25)')
  grad.addColorStop(0.5, 'rgba(255, 245, 248, 0.1)')
  grad.addColorStop(1, 'rgba(255, 255, 255, 0)')
  ctx.fillStyle = grad
  ctx.fillRect(-W, -H, W * 3, H * 3)

  drawGround(cx, by)

  if (props.stage === 'SEED') {
    drawSeedShape(cx, by)
  } else if (props.stage === 'SPROUT') {
    drawSproutStem(cx, by)
  } else {
    const top = drawTrunk(cx, by, stg.trunkH, stg.trunkW)

    globalPtIdx = 0
    termPoints = []
    leafHits = []

    if (stg.branchD > 0) {
      const spread = Math.PI * 0.5
      for (let i = 0; i < stg.mainB; i++) {
        const angle = -Math.PI / 2 - spread / 2 + (spread / Math.max(1, stg.mainB - 1)) * i
        drawBranches(top.x, top.y, stg.branchLen, angle, 0, stg.branchD, 5000 + i * 1000)
      }
    }

    if (termPoints.length > 0) {
      // Deterministic shuffle so leaves are placed consistently
      const sorted = [...termPoints].sort((a, b) => srand(a.idx * 777) - srand(b.idx * 777))

      const events = props.events || []
      // One leaf per event (capped by available terminal points)
      const eventLeavesToDraw = Math.min(events.length, sorted.length)

      // Leaf size scales with stage and fullscreen
      const baseLeafSize = isFullscreen.value ? 22 : 14
      const leafSize = baseLeafSize + (stg.branchD >= 3 ? 4 : 0)

      for (let i = 0; i < eventLeavesToDraw; i++) {
        const pt = sorted[i]
        drawEventLeaf(pt, events[i], i, leafSize)
      }

      // Draw decorative leaves on remaining terminal points (up to leafCount)
      const decorativeCount = Math.min(stg.maxLeaves, props.leafCount - eventLeavesToDraw, sorted.length - eventLeavesToDraw)
      for (let i = 0; i < decorativeCount; i++) {
        const pt = sorted[eventLeavesToDraw + i]
        drawDecorativeLeaf(
          pt.x, pt.y,
          pt.angle - Math.PI / 2 + (srand(pt.idx * 333) - 0.5) * 0.6,
          7 + srand(pt.idx * 444) * 5,
        )
      }

      // Draw flowers (on unused terminal points)
      if (stg.maxFlowers > 0 && props.flowerCount > 0) {
        const flowerStart = eventLeavesToDraw + decorativeCount
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

        // Draw fruits for FRUIT stage
        if (stg.hasFruit) {
          const fruitStart = flowerStart + Math.min(stg.maxFlowers, props.flowerCount)
          const fruitsToDraw = Math.min(6, sorted.length - fruitStart)
          for (let i = 0; i < fruitsToDraw; i++) {
            const pt = sorted[fruitStart + i]
            drawFruitAt(pt.x, pt.y)
          }
        }
      }
    }
  }

  ctx.restore()

  // Particles (drawn in screen space, not affected by zoom/pan)
  drawSparkles()

  if (props.stage === 'BLOOM' || props.stage === 'FRUIT') {
    updateAndDrawPetals()
  }

  animId = requestAnimationFrame(render)
}

// =============================================================
// EVENT HANDLING
// =============================================================

function getCanvasPos(e: MouseEvent | Touch): { x: number; y: number } {
  const canvas = canvasRef.value!
  const rect = canvas.getBoundingClientRect()
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top,
  }
}

/** Convert a screen position to canvas (logical) coordinates, accounting for zoom/pan */
function screenToCanvas(sx: number, sy: number): { x: number; y: number } {
  if (!isFullscreen.value || (zoom.value === 1 && panX.value === 0 && panY.value === 0)) {
    return { x: sx, y: sy }
  }
  const cx = W / 2
  const by = H - 50
  // Inverse of: translate(panX, panY) translate(cx, by-50) scale(zoom) translate(-cx, -(by-50))
  const x = (sx - panX.value - cx) / zoom.value + cx
  const y = (sy - panY.value - (by - 50)) / zoom.value + (by - 50)
  return { x, y }
}

function findLeafAt(sx: number, sy: number): LeafHit | null {
  const { x, y } = screenToCanvas(sx, sy)
  for (const hit of leafHits) {
    const dx = hit.x - x
    const dy = hit.y - y
    if (dx * dx + dy * dy <= hit.size * hit.size) {
      return hit
    }
  }
  return null
}

function handleCanvasClick(e: MouseEvent) {
  if (panMoved) {
    panMoved = false
    return
  }
  const pos = getCanvasPos(e)
  const hit = findLeafAt(pos.x, pos.y)
  if (hit) {
    emit('leaf-click', hit.eventId)
  }
}

function handleCanvasMove(e: MouseEvent) {
  if (isPanning) return
  const pos = getCanvasPos(e)
  const hit = findLeafAt(pos.x, pos.y)
  if (hit) {
    hoveredLeafIdx.value = leafHits.findIndex((h) => h.eventId === hit.eventId)
    const containerRect = containerRef.value?.getBoundingClientRect()
    hoverTooltip.value = {
      visible: true,
      x: pos.x,
      y: pos.y - 16,
      text: hit.title,
    }
    if (containerRect) {
      // Position tooltip relative to container
      hoverTooltip.value.x = e.clientX - containerRect.left
      hoverTooltip.value.y = e.clientY - containerRect.top - 16
    }
    canvasRef.value!.style.cursor = 'pointer'
  } else {
    hoveredLeafIdx.value = -1
    hoverTooltip.value.visible = false
    canvasRef.value!.style.cursor = isFullscreen.value ? (isPanning ? 'grabbing' : 'grab') : 'default'
  }
}

function handleCanvasLeave() {
  hoveredLeafIdx.value = -1
  hoverTooltip.value.visible = false
}

function handleWheel(e: WheelEvent) {
  if (!isFullscreen.value) return
  const delta = e.deltaY > 0 ? 1 / 1.1 : 1.1
  const newZoom = Math.max(0.5, Math.min(4, zoom.value * delta))
  zoom.value = newZoom
}

function handlePanStart(e: MouseEvent) {
  if (!isFullscreen.value) return
  isPanning = true
  panMoved = false
  panStartX = e.clientX
  panStartY = e.clientY
  panOriginX = panX.value
  panOriginY = panY.value
  canvasRef.value!.style.cursor = 'grabbing'
}

// Pan handlers attached at window level (set up in setupCanvas/onMounted)
function onPanMove(e: MouseEvent) {
  if (!isPanning) return
  const dx = e.clientX - panStartX
  const dy = e.clientY - panStartY
  if (Math.abs(dx) + Math.abs(dy) > 4) panMoved = true
  panX.value = panOriginX + dx
  panY.value = panOriginY + dy
}

function onPanEnd() {
  if (isPanning) {
    isPanning = false
    if (canvasRef.value) {
      canvasRef.value.style.cursor = isFullscreen.value ? 'grab' : 'default'
    }
  }
}

function handleTouchStart(e: TouchEvent) {
  if (!isFullscreen.value) return
  if (e.touches.length === 1) {
    isPanning = true
    panMoved = false
    panStartX = e.touches[0].clientX
    panStartY = e.touches[0].clientY
    panOriginX = panX.value
    panOriginY = panY.value
  } else if (e.touches.length === 2) {
    isPanning = false
    pinchStartDist = touchDist(e)
    pinchStartZoom = zoom.value
  }
}

function handleTouchMove(e: TouchEvent) {
  if (!isFullscreen.value) return
  if (e.touches.length === 1 && isPanning) {
    const dx = e.touches[0].clientX - panStartX
    const dy = e.touches[0].clientY - panStartY
    if (Math.abs(dx) + Math.abs(dy) > 4) panMoved = true
    panX.value = panOriginX + dx
    panY.value = panOriginY + dy
  } else if (e.touches.length === 2) {
    const dist = touchDist(e)
    if (pinchStartDist > 0) {
      const ratio = dist / pinchStartDist
      zoom.value = Math.max(0.5, Math.min(4, pinchStartZoom * ratio))
    }
  }
}

function handleTouchEnd() {
  isPanning = false
  pinchStartDist = 0
}

function touchDist(e: TouchEvent): number {
  const dx = e.touches[0].clientX - e.touches[1].clientX
  const dy = e.touches[0].clientY - e.touches[1].clientY
  return Math.sqrt(dx * dx + dy * dy)
}

// =============================================================
// FULLSCREEN & ZOOM CONTROLS
// =============================================================

function enterFullscreen() {
  isFullscreen.value = true
  zoom.value = 1
  panX.value = 0
  panY.value = 0
  emit('fullscreen-change', true)
  // Resize after DOM update
  nextTick(() => onResize())
}

function exitFullscreen() {
  isFullscreen.value = false
  zoom.value = 1
  panX.value = 0
  panY.value = 0
  emit('fullscreen-change', false)
  nextTick(() => onResize())
}

function zoomBy(factor: number) {
  zoom.value = Math.max(0.5, Math.min(4, zoom.value * factor))
}

function resetView() {
  zoom.value = 1
  panX.value = 0
  panY.value = 0
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

  ctx = canvas.getContext('2d')!
  if (!ctx) return

  ctx.scale(dpr, dpr)

  initSparkles()
  initPetals()

  if (animId) cancelAnimationFrame(animId)
  animId = requestAnimationFrame(render)
}

function onResize() {
  if (animId) cancelAnimationFrame(animId)

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

  ctx = canvas.getContext('2d')!
  if (!ctx) return
  ctx.scale(dpr, dpr)

  initSparkles()
  initPetals()

  animId = requestAnimationFrame(render)
}

// Watch events prop to pre-load images
watch(
  () => props.events,
  (events) => {
    if (!events) return
    for (const ev of events) {
      if (ev.images && ev.images.length > 0) {
        getImage(ev.images[0])
      }
    }
  },
  { immediate: true, deep: true },
)

onMounted(() => {
  setupCanvas()
  window.addEventListener('resize', onResize)
  window.addEventListener('mousemove', onPanMove)
  window.addEventListener('mouseup', onPanEnd)
})

onUnmounted(() => {
  if (animId) cancelAnimationFrame(animId)
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousemove', onPanMove)
  window.removeEventListener('mouseup', onPanEnd)
})
</script>

<style scoped>
.canvas-root {
  width: 100%;
  height: 100%;
}

.canvas-wrapper {
  width: 100%;
  height: 100%;
  min-height: 400px;
  max-height: 500px;
  position: relative;
  overflow: hidden;
  border-radius: var(--border-radius);
  cursor: default;
}

.canvas-wrapper.is-fullscreen {
  position: fixed;
  inset: 0;
  z-index: 9998;
  width: 100vw;
  height: 100vh;
  max-height: none;
  border-radius: 0;
  background: linear-gradient(180deg, #fdf6f0 0%, #fbe9ec 100%);
  cursor: grab;
}

.canvas-wrapper.is-fullscreen:active {
  cursor: grabbing;
}

canvas {
  display: block;
  width: 100%;
  height: 100%;
  touch-action: none;
}

/* Controls overlay (inline view) */
.canvas-controls {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  z-index: 2;
}

/* Fullscreen controls */
.fs-controls {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  z-index: 2;
}

.fs-zoom-info {
  padding: 6px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  font-size: 13px;
  color: #6a5a4a;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.fs-btns {
  display: flex;
  gap: 8px;
}

.ctrl-btn {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  color: #6a5a4a;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ctrl-btn:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-1px);
  color: var(--color-primary, #c9a86c);
  box-shadow: 0 4px 14px rgba(201, 168, 108, 0.25);
}

.ctrl-btn svg {
  width: 18px;
  height: 18px;
}

/* Hover tooltip */
.leaf-tooltip {
  position: absolute;
  z-index: 3;
  transform: translate(-50%, -100%);
  padding: 6px 12px;
  background: rgba(45, 35, 30, 0.92);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  border-radius: 8px;
  white-space: nowrap;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.leaf-tooltip::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid rgba(45, 35, 30, 0.92);
}

/* Mobile responsive */
@media (max-width: 768px) {
  .canvas-wrapper {
    min-height: 320px;
  }

  .ctrl-btn {
    width: 34px;
    height: 34px;
  }

  .ctrl-btn svg {
    width: 16px;
    height: 16px;
  }

  .fs-controls {
    top: 12px;
    right: 12px;
  }

  .fs-btns {
    flex-direction: column;
  }
}
</style>
