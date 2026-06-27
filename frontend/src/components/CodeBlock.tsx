/** Shows text as code with a line-number gutter. Used by the file viewer, the old
 * code panel, and the AI draft panel so code looks the same everywhere. */
export function CodeBlock({ text }: { text: string }) {
  const lines = text.split('\n');
  return (
    <pre className="code-view">
      <code>
        {lines.map((line, index) => (
          // A static render of file lines, so the index key is fine here.
          <span className="code-view__line" key={index}>
            <span className="code-view__num">{index + 1}</span>
            <span className="code-view__text">{line === '' ? ' ' : line}</span>
          </span>
        ))}
      </code>
    </pre>
  );
}
