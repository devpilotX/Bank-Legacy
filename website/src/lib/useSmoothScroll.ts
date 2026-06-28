import { useEffect } from 'react';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import Lenis from 'lenis';

gsap.registerPlugin(ScrollTrigger);

// Smooth scrolling with Lenis, kept in step with GSAP's ScrollTrigger so the two never
// fight each other. Clicking a nav link scrolls smoothly to the section and stops clear
// of the fixed nav. When motion is reduced we never start any of this, so scrolling
// stays plain and native.
export function useSmoothScroll(enabled: boolean) {
  useEffect(() => {
    if (!enabled) return;

    // The offset keeps a clicked section's heading from hiding under the fixed nav.
    const lenis = new Lenis({ anchors: { offset: -80 } });

    // Recalculate ScrollTrigger on every Lenis scroll.
    lenis.on('scroll', ScrollTrigger.update);

    // Drive Lenis from GSAP's ticker so both run on the same clock.
    const update = (time: number) => lenis.raf(time * 1000);
    gsap.ticker.add(update);
    gsap.ticker.lagSmoothing(0);

    return () => {
      gsap.ticker.remove(update);
      lenis.destroy();
    };
  }, [enabled]);
}
