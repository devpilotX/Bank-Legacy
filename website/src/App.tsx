import { Button } from '@/components/ui/button';

// Temporary check that shadcn/ui + the theme render correctly. Replaced by the
// real site once the base is proven solid.
export function App() {
  return (
    <div className="grid min-h-screen place-items-center gap-4 bg-background">
      <p className="text-2xl font-semibold text-foreground">Base check</p>
      <div className="flex gap-3">
        <Button>Book a call</Button>
        <Button variant="outline">Learn more</Button>
        <Button variant="link">Talk to us</Button>
      </div>
    </div>
  );
}
