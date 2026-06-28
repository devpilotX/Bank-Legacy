import { useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Float, RoundedBox } from '@react-three/drei';
import type { Group, Mesh } from 'three';

// The one 3D moment. It echoes our logo in space: an outlined "old" shell slowly giving
// way to a solid, modern core in the brand blue. Everything moves slowly and quietly,
// with no fast spinning and no flashing, because this runs in front of bankers. The
// scene is deliberately small so it stays smooth on an average laptop.
//
// This module is loaded only when we decide to show the 3D (see useEnable3D), so its
// weight never lands on visitors who get the static panel.

// A muted slate for the old shell and the brand blue for the modern core. These are
// picked to match the page, not loaded from CSS, since three needs plain color values.
const SHELL_COLOR = '#94a3b8';
const CORE_COLOR = '#3b4da6';

function CoreObject() {
  const group = useRef<Group>(null);
  const shell = useRef<Mesh>(null);

  // Time-based so the motion is the same speed on any screen. Slow on purpose.
  useFrame((state) => {
    const t = state.clock.getElapsedTime();
    if (group.current) {
      group.current.rotation.y = t * 0.15;
    }
    if (shell.current) {
      shell.current.rotation.x = t * 0.05;
      shell.current.rotation.y = -t * 0.08;
    }
  });

  return (
    <group ref={group}>
      {/* The old shell: a quiet wireframe box around the core. */}
      <mesh ref={shell}>
        <boxGeometry args={[2.6, 2.6, 2.6]} />
        <meshBasicMaterial color={SHELL_COLOR} wireframe transparent opacity={0.35} />
      </mesh>
      {/* The modern core: one solid, rounded block. */}
      <RoundedBox args={[1.5, 1.5, 1.5]} radius={0.18} smoothness={4}>
        <meshStandardMaterial color={CORE_COLOR} metalness={0.15} roughness={0.45} />
      </RoundedBox>
    </group>
  );
}

export default function HeroScene() {
  return (
    <div className="aspect-square w-full">
      <Canvas
        dpr={[1, 1.5]}
        camera={{ position: [0, 0, 5], fov: 40 }}
        gl={{ antialias: true, alpha: true }}
      >
        <ambientLight intensity={0.7} />
        <directionalLight position={[4, 5, 3]} intensity={1.1} />
        <directionalLight position={[-3, -2, -4]} intensity={0.3} />
        <Float speed={1.1} rotationIntensity={0.4} floatIntensity={0.7}>
          <CoreObject />
        </Float>
      </Canvas>
    </div>
  );
}
