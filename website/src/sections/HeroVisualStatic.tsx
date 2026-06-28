// A calm, static picture of the idea: old COBOL becoming clean, modern Java. It shows
// on its own for phones, low-power devices, and anyone who prefers reduced motion, and
// it sits under the 3D version as the fallback while that loads. It is marked
// aria-hidden because it is decoration; the words around it carry the meaning.
export function HeroVisualStatic() {
  return (
    <div
      aria-hidden="true"
      className="rounded-xl border border-border bg-card p-6 shadow-sm sm:p-8"
    >
      <div className="space-y-5 font-mono text-xs sm:text-sm">
        <div>
          <p className="mb-2 text-[10px] font-medium tracking-[0.2em] text-muted-foreground uppercase">
            COBOL
          </p>
          <div className="space-y-1.5 text-muted-foreground/70">
            <p>IDENTIFICATION DIVISION.</p>
            <p>PROGRAM-ID. LEDGER-POST.</p>
            <p>PERFORM POST-INTEREST.</p>
          </div>
        </div>

        <div className="flex items-center gap-3 text-muted-foreground">
          <span className="h-px flex-1 bg-border" />
          <span className="text-[10px] tracking-[0.2em] uppercase">becomes</span>
          <span className="h-px flex-1 bg-border" />
        </div>

        <div>
          <p className="mb-2 text-[10px] font-medium tracking-[0.2em] text-primary uppercase">
            Java
          </p>
          <div className="space-y-1.5 text-foreground">
            <p>class LedgerPosting {'{'}</p>
            <p className="pl-4 text-muted-foreground">void postInterest() {'{ ... }'}</p>
            <p>{'}'}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
