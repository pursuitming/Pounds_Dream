<template>
  <div class="body-avatar-container">
    <div ref="containerRef" class="body-avatar-3d"></div>
    <!-- 数据气泡 -->
    <div class="data-bubbles">
      <div class="bubble bubble-weight" v-if="weight">
        <span class="bubble-icon">⚖️</span>
        <span class="bubble-value">{{ weight }}kg</span>
      </div>
      <div class="bubble bubble-bmi" v-if="bmi">
        <span class="bubble-icon">💪</span>
        <span class="bubble-value">BMI {{ bmi }}</span>
      </div>
      <div class="bubble bubble-height" v-if="height">
        <span class="bubble-icon">📏</span>
        <span class="bubble-value">{{ height }}cm</span>
      </div>
    </div>
    <!-- 性别标识 -->
    <div class="gender-badge" v-if="gender">
      {{ gender === 1 ? '♂ 男生' : '♀ 女生' }}
    </div>
    <!-- 操作提示 -->
    <div class="controls-hint">
      <span>🖱️ 拖拽旋转 · 滚轮缩放</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

const props = defineProps({
  bmi: { type: Number, default: 22 },
  weight: { type: Number, default: 55 },
  height: { type: Number, default: 160 },
  gender: { type: Number, default: 1 }, // 1=男，2=女
  chest: { type: Number, default: null },
  waist: { type: Number, default: null },
  hip: { type: Number, default: null }
})

const containerRef = ref(null)
let scene, camera, renderer, controls, animationId
let bodyGroup = null
let isWaving = false
let isMale = true

// 男性配色方案
const MALE_COLORS = {
  skin: 0xffe4d6,
  hair: 0x4a3728,
  eye: 0x4a8fb5,
  top: 0x5b8def,
  topShadow: 0x4a7dde,
  bottom: 0x3d5a80,
  bottomShadow: 0x2d4a70,
  shoe: 0x2d3436,
  accent: 0x5b8def
}

// 女性配色方案
const FEMALE_COLORS = {
  skin: 0xfff0ec,
  hair: 0x8B6F47,
  eye: 0x6ab0d4,
  top: 0x9b7fe6,
  topShadow: 0x8a6fd6,
  bottom: 0xff6b9d,
  bottomShadow: 0xe85a8a,
  shoe: 0xffffff,
  accent: 0xff9db5
}

// 根据 BMI 计算体型参数
function calcBodyParams() {
  const bmi = props.bmi || 22
  const height = props.height || 160
  const scale = height / 160
  const bmiFactor = bmi / 22

  let bodyWidth = bmiFactor
  let waistWidth = bmiFactor

  if (props.chest && props.waist && props.hip) {
    bodyWidth = (props.chest / 85 + props.hip / 92) / 2
    waistWidth = props.waist / 68
  }

  // 男性肩膀更宽，女性腰部更细
  const shoulderFactor = isMale ? 1.15 : 1.0
  const waistFactor = isMale ? 1.0 : 0.85

  return {
    scale,
    bodyWidth: Math.max(0.75, Math.min(1.4, bodyWidth)) * shoulderFactor,
    waistWidth: Math.max(0.7, Math.min(1.3, waistWidth)) * waistFactor,
    hipWidth: isMale ? 1.0 : 1.1,
    bmi
  }
}

// 创建材质
function createMaterials() {
  const colors = isMale ? MALE_COLORS : FEMALE_COLORS

  return {
    skin: new THREE.MeshStandardMaterial({
      color: colors.skin,
      roughness: 0.6,
      metalness: 0.05
    }),
    hair: new THREE.MeshStandardMaterial({
      color: colors.hair,
      roughness: 0.7,
      metalness: 0.1
    }),
    top: new THREE.MeshStandardMaterial({
      color: colors.top,
      roughness: 0.5,
      metalness: 0.05
    }),
    bottom: new THREE.MeshStandardMaterial({
      color: colors.bottom,
      roughness: 0.5,
      metalness: 0.05
    }),
    shoe: new THREE.MeshStandardMaterial({
      color: colors.shoe,
      roughness: 0.4,
      metalness: 0.1
    }),
    eyeWhite: new THREE.MeshStandardMaterial({
      color: 0xffffff,
      roughness: 0.3,
      metalness: 0.0
    }),
    eye: new THREE.MeshStandardMaterial({
      color: colors.eye,
      roughness: 0.4,
      metalness: 0.2
    }),
    pupil: new THREE.MeshStandardMaterial({
      color: 0x2d3436,
      roughness: 0.3,
      metalness: 0.3
    }),
    mouth: new THREE.MeshStandardMaterial({
      color: 0xff6b8a,
      roughness: 0.5,
      metalness: 0.0
    }),
    blush: new THREE.MeshStandardMaterial({
      color: 0xffb6c1,
      roughness: 0.8,
      metalness: 0.0,
      transparent: true,
      opacity: isMale ? 0.3 : 0.5
    })
  }
}

// 创建完整的人物模型
function createBodyModel() {
  isMale = props.gender === 1

  if (bodyGroup) {
    scene.remove(bodyGroup)
    bodyGroup.traverse(child => {
      if (child.geometry) child.geometry.dispose()
    })
  }

  bodyGroup = new THREE.Group()
  const params = calcBodyParams()
  const materials = createMaterials()
  const s = params.scale

  // ========== 头部 ==========
  const headGroup = createHead(params, materials)
  headGroup.position.y = 1.65 * s
  bodyGroup.add(headGroup)

  // ========== 脖子 ==========
  const neck = new THREE.Mesh(
    new THREE.CylinderGeometry(0.06 * s, 0.07 * s, 0.12 * s, 16),
    materials.skin
  )
  neck.position.y = 1.45 * s
  bodyGroup.add(neck)

  // ========== 躯干 ==========
  const torsoGroup = createTorso(params, materials)
  bodyGroup.add(torsoGroup)

  // ========== 左臂 ==========
  const leftArm = createArm(params, materials)
  leftArm.position.set(0.25 * s, 1.25 * s, 0)
  leftArm.name = 'leftArm'
  bodyGroup.add(leftArm)

  // ========== 右臂 ==========
  const rightArm = createArm(params, materials)
  rightArm.position.set(-0.25 * s, 1.25 * s, 0)
  rightArm.name = 'rightArm'
  bodyGroup.add(rightArm)

  // ========== 左腿 ==========
  const leftLeg = createLeg(params, materials)
  leftLeg.position.set(0.09 * s, 0.45 * s, 0)
  bodyGroup.add(leftLeg)

  // ========== 右腿 ==========
  const rightLeg = createLeg(params, materials)
  rightLeg.position.set(-0.09 * s, 0.45 * s, 0)
  bodyGroup.add(rightLeg)

  // ========== 地面阴影 ==========
  const shadowGeom = new THREE.CircleGeometry(0.3 * s, 32)
  const shadowMat = new THREE.MeshBasicMaterial({
    color: isMale ? 0x5b8def : 0x9b7fe6,
    transparent: true,
    opacity: 0.15
  })
  const shadow = new THREE.Mesh(shadowGeom, shadowMat)
  shadow.rotation.x = -Math.PI / 2
  shadow.position.y = 0.01
  bodyGroup.add(shadow)

  scene.add(bodyGroup)
  return bodyGroup
}

// 创建头部（根据性别不同）
function createHead(params, materials) {
  const headGroup = new THREE.Group()
  const s = params.scale

  // 头 - 圆润的大脑袋
  const head = new THREE.Mesh(
    new THREE.SphereGeometry(0.28 * s, 32, 32),
    materials.skin
  )
  headGroup.add(head)

  if (isMale) {
    // ========== 男性发型 ==========
    // 短发 - 顶部
    const hairTop = new THREE.Mesh(
      new THREE.SphereGeometry(0.29 * s, 32, 32, 0, Math.PI * 2, 0, Math.PI * 0.35),
      materials.hair
    )
    hairTop.position.y = 0.04 * s
    headGroup.add(hairTop)

    // 短发 - 两侧
    const hairSide = new THREE.CapsuleGeometry(0.05 * s, 0.12 * s, 8, 12)
    const hairSideL = new THREE.Mesh(hairSide, materials.hair)
    hairSideL.position.set(-0.22 * s, -0.05 * s, 0)
    headGroup.add(hairSideL)

    const hairSideR = new THREE.Mesh(hairSide, materials.hair)
    hairSideR.position.set(0.22 * s, -0.05 * s, 0)
    headGroup.add(hairSideR)
  } else {
    // ========== 女性发型 ==========
    // 长发 - 顶部蓬松
    const hairTop = new THREE.Mesh(
      new THREE.SphereGeometry(0.3 * s, 32, 32, 0, Math.PI * 2, 0, Math.PI * 0.45),
      materials.hair
    )
    hairTop.position.y = 0.03 * s
    headGroup.add(hairTop)

    // 长发 - 两侧垂下
    const hairLongGeom = new THREE.CapsuleGeometry(0.07 * s, 0.35 * s, 8, 16)
    const hairLongL = new THREE.Mesh(hairLongGeom, materials.hair)
    hairLongL.position.set(-0.22 * s, -0.2 * s, 0)
    headGroup.add(hairLongL)

    const hairLongR = new THREE.Mesh(hairLongGeom, materials.hair)
    hairLongR.position.set(0.22 * s, -0.2 * s, 0)
    headGroup.add(hairLongR)

    // 刘海
    const bangs = new THREE.Mesh(
      new THREE.SphereGeometry(0.25 * s, 16, 16, 0, Math.PI, 0, Math.PI * 0.3),
      materials.hair
    )
    bangs.position.set(0, 0.08 * s, 0.12 * s)
    bangs.rotation.x = -0.3
    headGroup.add(bangs)

    // 发饰 - 蝴蝶结
    const bowGroup = createBow(params)
    bowGroup.position.set(0.22 * s, 0.15 * s, 0.08 * s)
    headGroup.add(bowGroup)
  }

  // 左眼
  const eyeGroupL = createEye(params, materials)
  eyeGroupL.position.set(-0.09 * s, 0.03 * s, 0.24 * s)
  headGroup.add(eyeGroupL)

  // 右眼
  const eyeGroupR = createEye(params, materials)
  eyeGroupR.position.set(0.09 * s, 0.03 * s, 0.24 * s)
  headGroup.add(eyeGroupR)

  // 腮红
  const blushGeom = new THREE.CircleGeometry(0.04 * s, 16)
  const blushL = new THREE.Mesh(blushGeom, materials.blush)
  blushL.position.set(-0.14 * s, -0.04 * s, 0.26 * s)
  headGroup.add(blushL)

  const blushR = new THREE.Mesh(blushGeom, materials.blush)
  blushR.position.set(0.14 * s, -0.04 * s, 0.26 * s)
  headGroup.add(blushR)

  // 嘴巴
  const mouthShape = new THREE.Shape()
  mouthShape.moveTo(-0.04 * s, 0)
  mouthShape.quadraticCurveTo(0, isMale ? -0.015 * s : -0.025 * s, 0.04 * s, 0)
  const mouthGeom = new THREE.ShapeGeometry(mouthShape)
  const mouth = new THREE.Mesh(mouthGeom, materials.mouth)
  mouth.position.set(0, -0.08 * s, 0.27 * s)
  headGroup.add(mouth)

  // 男性加眉毛
  if (isMale) {
    const browGeom = new THREE.CapsuleGeometry(0.015 * s, 0.04 * s, 4, 8)
    const browMat = new THREE.MeshStandardMaterial({ color: 0x3a2a1a, roughness: 0.8 })

    const browL = new THREE.Mesh(browGeom, browMat)
    browL.position.set(-0.09 * s, 0.1 * s, 0.26 * s)
    browL.rotation.z = 0.1
    headGroup.add(browL)

    const browR = new THREE.Mesh(browGeom, browMat)
    browR.position.set(0.09 * s, 0.1 * s, 0.26 * s)
    browR.rotation.z = -0.1
    headGroup.add(browR)
  }

  return headGroup
}

// 创建眼睛
function createEye(params, materials) {
  const eyeGroup = new THREE.Group()
  const s = params.scale
  const eyeSize = isMale ? 0.045 : 0.055

  // 眼白
  const eyeWhite = new THREE.Mesh(
    new THREE.SphereGeometry(eyeSize * s, 16, 16),
    materials.eyeWhite
  )
  eyeWhite.scale.z = 0.6
  eyeGroup.add(eyeWhite)

  // 虹膜
  const iris = new THREE.Mesh(
    new THREE.SphereGeometry(eyeSize * 0.7 * s, 16, 16),
    materials.eye
  )
  iris.position.z = 0.02 * s
  eyeGroup.add(iris)

  // 瞳孔
  const pupil = new THREE.Mesh(
    new THREE.SphereGeometry(eyeSize * 0.45 * s, 16, 16),
    materials.pupil
  )
  pupil.position.z = 0.03 * s
  eyeGroup.add(pupil)

  // 高光
  const highlight = new THREE.Mesh(
    new THREE.SphereGeometry(eyeSize * 0.25 * s, 8, 8),
    new THREE.MeshBasicMaterial({ color: 0xffffff })
  )
  highlight.position.set(eyeSize * 0.3 * s, eyeSize * 0.3 * s, 0.04 * s)
  eyeGroup.add(highlight)

  return eyeGroup
}

// 创建躯干（根据性别不同）
function createTorso(params, materials) {
  const torsoGroup = new THREE.Group()
  const s = params.scale
  const colors = isMale ? MALE_COLORS : FEMALE_COLORS

  // 上身
  const upperTorso = new THREE.Mesh(
    new THREE.CapsuleGeometry(0.18 * s * params.bodyWidth, 0.3 * s, 8, 16),
    materials.top
  )
  upperTorso.position.y = 1.2 * s
  torsoGroup.add(upperTorso)

  // 腰部
  const waist = new THREE.Mesh(
    new THREE.CylinderGeometry(
      0.14 * s * params.waistWidth,
      0.18 * s * params.bodyWidth,
      0.15 * s,
      16
    ),
    materials.top
  )
  waist.position.y = 0.95 * s
  torsoGroup.add(waist)

  if (isMale) {
    // 男性 - 裤子
    const pants = new THREE.Mesh(
      new THREE.CylinderGeometry(
        0.17 * s * params.bodyWidth,
        0.15 * s,
        0.35 * s,
        16
      ),
      materials.bottom
    )
    pants.position.y = 0.65 * s
    torsoGroup.add(pants)

    // 腰带
    const belt = new THREE.Mesh(
      new THREE.CylinderGeometry(
        0.175 * s * params.bodyWidth,
        0.175 * s * params.bodyWidth,
        0.03 * s,
        16
      ),
      new THREE.MeshStandardMaterial({ color: 0x2d3436, roughness: 0.4, metalness: 0.3 })
    )
    belt.position.y = 0.85 * s
    torsoGroup.add(belt)
  } else {
    // 女性 - 裙子
    const skirt = new THREE.Mesh(
      new THREE.CylinderGeometry(
        0.18 * s * params.bodyWidth,
        0.28 * s * params.hipWidth,
        0.3 * s,
        16
      ),
      materials.bottom
    )
    skirt.position.y = 0.7 * s
    torsoGroup.add(skirt)

    // 裙摆花边
    const skirtRim = new THREE.Mesh(
      new THREE.TorusGeometry(0.28 * s * params.hipWidth, 0.015 * s, 8, 32),
      materials.bottom
    )
    skirtRim.position.y = 0.55 * s
    skirtRim.rotation.x = Math.PI / 2
    torsoGroup.add(skirtRim)
  }

  return torsoGroup
}

// 创建手臂
function createArm(params, materials) {
  const armGroup = new THREE.Group()
  const s = params.scale

  // 上臂
  const upperArm = new THREE.Mesh(
    new THREE.CapsuleGeometry(0.045 * s, 0.15 * s, 8, 12),
    materials.top
  )
  armGroup.add(upperArm)

  // 前臂
  const forearm = new THREE.Mesh(
    new THREE.CapsuleGeometry(0.04 * s, 0.12 * s, 8, 12),
    materials.skin
  )
  forearm.position.y = -0.18 * s
  armGroup.add(forearm)

  // 手
  const hand = new THREE.Mesh(
    new THREE.SphereGeometry(0.04 * s, 12, 12),
    materials.skin
  )
  hand.position.y = -0.28 * s
  hand.scale.set(1, 0.8, 0.7)
  armGroup.add(hand)

  return armGroup
}

// 创建腿部
function createLeg(params, materials) {
  const legGroup = new THREE.Group()
  const s = params.scale

  if (isMale) {
    // 男性 - 裤腿
    const leg = new THREE.Mesh(
      new THREE.CapsuleGeometry(0.06 * s, 0.35 * s, 8, 12),
      materials.bottom
    )
    legGroup.add(leg)
  } else {
    // 女性 - 裸腿
    const leg = new THREE.Mesh(
      new THREE.CapsuleGeometry(0.055 * s, 0.3 * s, 8, 12),
      materials.skin
    )
    legGroup.add(leg)
  }

  // 鞋子
  const shoe = new THREE.Mesh(
    new THREE.CapsuleGeometry(0.05 * s, 0.08 * s, 8, 12),
    materials.shoe
  )
  shoe.position.y = isMale ? -0.35 * s : -0.3 * s
  shoe.scale.set(1, 0.7, 1.3)
  legGroup.add(shoe)

  return legGroup
}

// 创建蝴蝶结（女性专用）
function createBow(params) {
  const bowGroup = new THREE.Group()
  const s = params.scale
  const bowMat = new THREE.MeshStandardMaterial({
    color: 0xff69b4,
    roughness: 0.4,
    metalness: 0.1
  })

  // 左翅膀
  const wingL = new THREE.Mesh(
    new THREE.SphereGeometry(0.035 * s, 12, 12),
    bowMat
  )
  wingL.position.set(-0.03 * s, 0, 0)
  wingL.scale.set(1.2, 0.8, 0.5)
  bowGroup.add(wingL)

  // 右翅膀
  const wingR = new THREE.Mesh(
    new THREE.SphereGeometry(0.035 * s, 12, 12),
    bowMat
  )
  wingR.position.set(0.03 * s, 0, 0)
  wingR.scale.set(1.2, 0.8, 0.5)
  bowGroup.add(wingR)

  // 中心结
  const center = new THREE.Mesh(
    new THREE.SphereGeometry(0.02 * s, 8, 8),
    bowMat
  )
  bowGroup.add(center)

  return bowGroup
}

// 挥手动画
function startWaveAnimation() {
  if (isWaving) return
  isWaving = true

  const rightArm = bodyGroup.getObjectByName('rightArm')
  if (!rightArm) {
    isWaving = false
    return
  }

  const duration = 1200
  const startTime = Date.now()
  const startRotation = rightArm.rotation.z

  function animateWave() {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 挥手动画
    rightArm.rotation.z = startRotation + Math.sin(progress * Math.PI * 5) * 0.6 - 1.5

    if (progress < 1) {
      requestAnimationFrame(animateWave)
    } else {
      rightArm.rotation.z = startRotation
      isWaving = false
    }
  }

  animateWave()
}

function initScene() {
  const container = containerRef.value
  if (!container) return

  // 场景
  scene = new THREE.Scene()

  // 相机
  camera = new THREE.PerspectiveCamera(35, container.clientWidth / container.clientHeight, 0.1, 100)
  camera.position.set(0, 1.2, 3)

  // 渲染器
  renderer = new THREE.WebGLRenderer({
    antialias: true,
    alpha: true
  })
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2
  container.appendChild(renderer.domElement)

  // 轨道控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.enablePan = false
  controls.minDistance = 1.5
  controls.maxDistance = 5
  controls.minPolarAngle = Math.PI * 0.2
  controls.maxPolarAngle = Math.PI * 0.8
  controls.target.set(0, 1, 0)

  // 灯光
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  const mainLight = new THREE.DirectionalLight(0xffffff, 0.8)
  mainLight.position.set(3, 5, 4)
  scene.add(mainLight)

  const fillLight = new THREE.DirectionalLight(0xffe4e1, 0.3)
  fillLight.position.set(-3, 3, -2)
  scene.add(fillLight)

  const rimLight = new THREE.DirectionalLight(isMale ? 0x5b8def : 0x9b7fe6, 0.4)
  rimLight.position.set(0, 3, -4)
  scene.add(rimLight)

  // 创建人体模型
  createBodyModel()

  // 点击事件
  container.addEventListener('click', handleClick)

  // 动画循环
  function animate() {
    animationId = requestAnimationFrame(animate)

    if (bodyGroup && !isWaving) {
      const time = Date.now() * 0.001
      bodyGroup.position.y = Math.sin(time * 1.5) * 0.01
    }

    controls.update()
    renderer.render(scene, camera)
  }
  animate()
}

// 点击处理
function handleClick(event) {
  const container = containerRef.value
  const rect = container.getBoundingClientRect()
  const mouse = new THREE.Vector2(
    ((event.clientX - rect.left) / rect.width) * 2 - 1,
    -((event.clientY - rect.top) / rect.height) * 2 + 1
  )

  const raycaster = new THREE.Raycaster()
  raycaster.setFromCamera(mouse, camera)

  const intersects = raycaster.intersectObjects(bodyGroup.children, true)
  if (intersects.length > 0) {
    startWaveAnimation()
  }
}

function handleResize() {
  const container = containerRef.value
  if (!container || !camera || !renderer) return

  camera.aspect = container.clientWidth / container.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(container.clientWidth, container.clientHeight)
}

onMounted(() => {
  initScene()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (controls) {
    controls.dispose()
  }
  if (renderer) {
    renderer.dispose()
    const container = containerRef.value
    if (container && renderer.domElement) {
      container.removeChild(renderer.domElement)
    }
  }
  if (bodyGroup) {
    bodyGroup.traverse(child => {
      if (child.geometry) child.geometry.dispose()
      if (child.material) {
        if (Array.isArray(child.material)) {
          child.material.forEach(m => m.dispose())
        } else {
          child.material.dispose()
        }
      }
    })
  }
})

// 监听属性变化
watch(() => [props.bmi, props.weight, props.height, props.gender, props.chest, props.waist, props.hip], () => {
  if (scene) {
    createBodyModel()
  }
})
</script>

<style scoped>
.body-avatar-container {
  position: relative;
  width: 100%;
  height: 400px;
  background: linear-gradient(180deg, #f8f0ff 0%, #fff0f5 50%, #fff 100%);
  border-radius: 12px;
  overflow: hidden;
}

.body-avatar-3d {
  width: 100%;
  height: 100%;
  cursor: grab;
}

.body-avatar-3d:active {
  cursor: grabbing;
}

.data-bubbles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.bubble {
  position: absolute;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 8px 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 20px rgba(155, 127, 230, 0.25);
  border: 2px solid rgba(255, 182, 193, 0.4);
  animation: bubble-float 3s ease-in-out infinite;
  font-size: 14px;
  font-weight: 600;
  color: #3d2b4a;
}

.bubble-weight {
  top: 15px;
  left: 15px;
  animation-delay: 0s;
}

.bubble-bmi {
  top: 15px;
  right: 15px;
  animation-delay: 0.5s;
}

.bubble-height {
  bottom: 60px;
  left: 15px;
  animation-delay: 1s;
}

.bubble-icon {
  font-size: 16px;
}

.gender-badge {
  position: absolute;
  top: 15px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #3d2b4a;
  box-shadow: 0 4px 20px rgba(155, 127, 230, 0.25);
  border: 2px solid rgba(255, 182, 193, 0.4);
}

@keyframes bubble-float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

.controls-hint {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  color: #9b8aad;
  padding: 6px 16px;
  border-radius: 15px;
  font-size: 12px;
  pointer-events: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
</style>
