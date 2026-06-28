import { useEffect } from 'react';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

// One gentle reveal, used the same way on every section: a soft fade with a small rise
// as the section comes into view. Subtle and consistent on purpose, so the page has a
// single feel rather than a different trick per section. When motion is reduced we skip
// it entirely, so everything is simply visible from the start.
export function useSectionReveals(enabled: boolean) {
  useEffect(() => {
    if (!enabled) return;

    const context = gsap.context(() => {
      const targets = gsap.utils.toArray<HTMLElement>('[data-reveal]');
      targets.forEach((element) => {
        gsap.fromTo(
          element,
          { autoAlpha: 0, y: 24 },
          {
            autoAlpha: 1,
            y: 0,
            duration: 0.7,
            ease: 'power2.out',
            scrollTrigger: { trigger: element, start: 'top 85%', once: true },
          },
        );
      });
    });

    return () => context.revert();
  }, [enabled]);
}
