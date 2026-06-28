import { useEffect, useState } from 'react';

// Tracks the visitor's "reduce motion" setting and keeps up with changes. When it is on
// we turn off smooth scroll, the reveal animations, and the 3D, and show the static
// version instead. That is both kinder and more professional.
export function useReducedMotion(): boolean {
  const [reduced, setReduced] = useState(
    () =>
      typeof window !== 'undefined' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches,
  );

  useEffect(() => {
    const query = window.matchMedia('(prefers-reduced-motion: reduce)');
    const onChange = () => setReduced(query.matches);
    query.addEventListener('change', onChange);
    return () => query.removeEventListener('change', onChange);
  }, []);

  return reduced;
}

type ConnectionLike = { saveData?: boolean };

// Decides whether to load the 3D hero. We only load it on a wide screen, with reduced
// motion off, on a device that looks capable enough. Phones, low-power devices, and
// reduced-motion visitors get the clean static panel instead. This also keeps the heavy
// 3D code off the critical path for everyone who does not see it.
export function useEnable3D(): boolean {
  const reduced = useReducedMotion();
  const [capable, setCapable] = useState(false);

  useEffect(() => {
    const wideEnough = window.matchMedia('(min-width: 1024px)');
    const cores = navigator.hardwareConcurrency ?? 4;
    const connection = (navigator as Navigator & { connection?: ConnectionLike }).connection;
    const saveData = connection?.saveData === true;

    const check = () => setCapable(wideEnough.matches && cores >= 4 && !saveData);
    check();
    wideEnough.addEventListener('change', check);
    return () => wideEnough.removeEventListener('change', check);
  }, []);

  return capable && !reduced;
}
