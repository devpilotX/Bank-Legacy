// Builds the project report as a real Word .docx file.
//
// How to run it:
//   cd report
//   npm install        (first time only, pulls in the docx package)
//   npm run build      (or: node generate-report.mjs)
//
// It writes Project-Report.docx next to this script. If you change the words, just run
// it again and the file is rebuilt. All the text lives in this one file, in the SECTIONS
// list near the bottom, so you can edit it without touching the layout code.
//
// House rules for the writing, kept on purpose: plain casual English, first person, short
// sentences, no em dash, no hype words, no emoji. If you add text, keep that voice.

import {
  Document,
  Packer,
  Paragraph,
  TextRun,
  HeadingLevel,
  AlignmentType,
  PageBreak,
  TableOfContents,
  Footer,
  PageNumber,
} from 'docx';
import { writeFileSync } from 'node:fs';

const TODAY = 'June 29, 2026';
const OUT = 'Project-Report.docx';

// ---- small helpers so the content below stays easy to read ----

// A normal paragraph of body text.
function p(text) {
  return new Paragraph({
    spacing: { after: 160, line: 276 },
    children: [new TextRun({ text })],
  });
}

// A bullet point.
function li(text) {
  return new Paragraph({
    bullet: { level: 0 },
    spacing: { after: 80, line: 276 },
    children: [new TextRun({ text })],
  });
}

// A big section heading. These are the ones that show up in the table of contents.
function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 320, after: 160 },
    children: [new TextRun({ text })],
  });
}

// A smaller heading inside a section.
function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 220, after: 100 },
    children: [new TextRun({ text })],
  });
}

// Turns a list of content "blocks" into paragraphs. Each block is either a string (body
// text), or a tagged object for headings and bullets. This keeps the SECTIONS list short.
function render(blocks) {
  const out = [];
  for (const block of blocks) {
    if (typeof block === 'string') {
      out.push(p(block));
    } else if (block.h2) {
      out.push(h2(block.h2));
    } else if (block.li) {
      out.push(li(block.li));
    }
  }
  return out;
}

// ---- the title page ----

function titlePage() {
  return [
    new Paragraph({ spacing: { before: 2600 } }),
    new Paragraph({
      heading: HeadingLevel.TITLE,
      alignment: AlignmentType.CENTER,
      children: [new TextRun({ text: 'The COBOL Modernization Project' })],
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { before: 200, after: 80 },
      children: [
        new TextRun({ text: 'A plain, honest report on what it is and where it really stands', italics: true, size: 26 }),
      ],
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { before: 600 },
      children: [new TextRun({ text: 'Working name: Corewise (still a placeholder)', size: 22 })],
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { before: 80 },
      children: [new TextRun({ text: TODAY, size: 22 })],
    }),
    new Paragraph({ children: [new PageBreak()] }),
  ];
}

// ---- the table of contents ----
// This is a real Word table of contents built from the big headings. When you open the
// file, Word may ask to update fields, or you can right click the list and choose Update
// Field, and the page numbers fill in.

function contents() {
  return [
    new Paragraph({
      heading: HeadingLevel.HEADING_1,
      spacing: { after: 160 },
      children: [new TextRun({ text: 'What is in here' })],
    }),
    new TableOfContents('Contents', { hyperlink: true, headingStyleRange: '1-1' }),
    new Paragraph({ children: [new PageBreak()] }),
  ];
}

// ---- the actual writing ----
// Each section is a title plus a list of blocks. Edit the words here.

const SECTIONS = [
  {
    title: '1. The big idea, in simple words',
    blocks: [
      'So here is the idea. Lots of small banks in the US still run on really old computer code called COBOL. It was written decades ago, it still runs the core of the bank every day, and it mostly works. The problem is the people who understand that code are getting old and retiring, and there are fewer of them every year.',
      'We help those banks. We read their old code, write down clearly how it works so it is not stuck in one person\u2019s head, and then we slowly rewrite it into modern code without breaking anything.',
      'The one line version is this: we help small US banks read and safely fix their old COBOL systems before the people who understand them retire.',
      'That is the whole business in a sentence. The rest of this report is just me explaining how it works, what is actually built today, and what is still left to do. I am going to be straight about all of it.',
    ],
  },
  {
    title: '2. Why this matters',
    blocks: [
      'A bank\u2019s core system is the thing that knows every account, every balance, and every transaction. If it goes down, the bank stops. People cannot get their money. So nobody wants to touch it.',
      'And that is the trap. The code still works, so for years it is easier to leave it alone. But the risk keeps growing quietly in the background, for a few reasons.',
      { li: 'The people who wrote it are retiring. When the last person who really understands it walks out the door, the knowledge goes with them.' },
      { li: 'The notes are thin. Often the code itself is the only real record of how the bank runs, and old COBOL is hard to read.' },
      { li: 'One wrong change can be expensive. A small mistake in the wrong place can post bad interest, or break a nightly run, and that costs real money and real trust.' },
      'So the bank is stuck between two scary options. Leave the old system alone and hope nobody important retires, or rip it out and replace it and pray nothing breaks. Both are bad. We are trying to offer a third option that is slow, careful, and safe.',
    ],
  },
  {
    title: '3. Who it is for',
    blocks: [
      'This is built for the smaller banks. Community banks, credit unions, and smaller regional banks. The kind of place where a few quiet people have kept the core running for years.',
      'The big software vendors chase the big banks, because that is where the big money is. The smaller core systems, the layer these smaller banks actually live in, mostly get ignored. We do not ignore it. That is the work we chose.',
      'If you are a smaller bank running an old core, and you are a little worried about who really understands it anymore, you are exactly who this is for.',
    ],
  },
  {
    title: '4. How the whole thing works, start to finish',
    blocks: [
      'There are two pieces to this project.',
      { li: 'The internal tool. This is the app our own team uses to do the work. The public never sees it.' },
      { li: 'The public website. This is the small, simple site that shows a bank we are real and lets them get in touch.' },
      'Most of the real work happens in the internal tool. Let me walk through what happens to a piece of old code when it comes in.',
      { h2: 'Step by step' },
      { li: 'It comes in. An engineer uploads the old COBOL files into a project for that bank. The tool notes what each file is, how big it is, and keeps a fingerprint of it.' },
      { li: 'The AI explains it. We ask an AI to read the code and write a plain explanation of what it does. An engineer reads that and checks it.' },
      { li: 'We map how it connects. The tool reads the code and finds the links, like which program calls which other program, and which shared record layouts they use. That gives a picture of how the parts fit together.' },
      { li: 'We rewrite it, one small piece at a time. The AI writes a first draft of the same logic in modern Java. An engineer reviews it, fixes it, and approves the final version. The old system keeps running the whole time.' },
      { li: 'We check the new code behaves like the old code. This is the verification step, and it is the most important promise. I will be honest later in this report about how much of it actually works today.' },
      'The key thing is that nothing is rushed and a person signs off on every step. We go slow on purpose, because it is a bank.',
    ],
  },
  {
    title: '5. The safe first job',
    blocks: [
      'A nervous bank is not going to let a stranger rewrite their core on day one. They should not. So the first job we offer is the safe one, and it touches nothing that is running.',
      'That first job is just reading and mapping. We take the old code, we use the AI to read it fast, our engineers make sense of it, and we write down clearly what each part does and how it all connects. The bank ends up with a clear picture of their own system, in plain words, instead of it being locked in one retiring person\u2019s memory.',
      'Nothing live is changed in this step. We are only reading. That is the point. It is genuinely useful on its own, and it lets the bank see how careful we are before we ever touch anything. By the time we talk about rewriting a piece, we have earned some trust.',
    ],
  },
  {
    title: '6. The AI part, explained simply',
    blocks: [
      'People hear AI and think one of two things. Either it is magic, or it is going to break everything. The truth here is in the middle, and how we use it is the important part.',
      'First, we rent the AI. We do not build or run our own. We send the code to a hosted AI service (we can use Claude or GPT) and it sends back an answer. That keeps things simpler and cheaper for now.',
      'Second, and this is the whole point, the AI never gets the final say. The AI is fast, and it is good at reading code and writing a first draft. But it can also miss a rule that only your bank knows, and say something wrong with total confidence. In a bank, fast but wrong is dangerous.',
      'So every single thing the AI produces is treated as a draft. A person has to read it and approve it. The explanation of the code is a draft until an engineer approves it. The Java translation is a draft until an engineer reviews and approves it. Even the test cases the AI suggests are just suggestions until a person confirms them.',
      'That human check is not a nice extra. It is the product. The AI gives us speed, the engineer gives us safety, and you need both. Speed alone is how you break a bank.',
    ],
  },
  {
    title: '7. The verification idea, and the honest truth about it today',
    blocks: [
      'Verification is the heart of what we sell, so I want to explain what it does, because this is the part we just made real.',
      { h2: 'What it is meant to do' },
      'The promise is simple to say. When we rewrite a piece of COBOL into Java, we want to prove the new Java behaves exactly like the old code. The way you do that is with test cases. A test case is a set of inputs. You run the old COBOL on those inputs to get the right answer, you run the new Java on the same inputs, and you check they match. Do that for enough cases and you can trust the new piece.',
      { h2: 'What it does now' },
      'It actually runs both sides. When you press run, the engine compiles and runs the original COBOL to get the true expected output, then compiles and runs the approved Java the same way, and compares the two. A person no longer types in the expected output. The old COBOL gives us the right answer. The AI can still suggest input cases, which a person confirms.',
      'I proved this end to end on a small interest program. The engine ran the COBOL and a correct Java and they matched, so it passed. Then I changed the Java to use the wrong rate, ran it again, and the engine caught the difference and showed exactly where the two outputs differed. That is the real thing working, not a stand in.',
      { h2: 'How it stays safe and fair' },
      'Each run happens in its own separate process, in a throwaway folder, with a hard time limit that stops a program that runs too long. The comparison has plain options you can see: trim trailing spaces, and allow a small numeric difference. Whatever was used is shown with the result. A difference that is only formatting is flagged apart from a real difference in behavior, so the two are never mixed up.',
      { h2: 'The honest limits' },
      'A few things to be straight about. The safety is process level, not a locked down container yet, so for production near a bank each run should later go inside stronger isolation. The checks are only as good as the inputs we give them, so a thin set of cases proves less. And it runs programs that read standard input and write standard output, and can stage input files, but a program made of several COBOL pieces that call each other would also need those pieces.',
    ],
  },
  {
    title: '8. The tech, in plain words',
    blocks: [
      'Here is what the project is built out of, and why, with no jargon. One or two lines each.',
      { li: 'Backend: Java with Spring Boot. This is the engine behind the internal tool. It takes in the code, talks to the AI, stores everything, and serves it up. Java and Spring Boot are a normal, boring, dependable choice for this, which is what you want for a bank.' },
      { li: 'The internal tool: React with IBM Carbon. React is a common way to build app screens. Carbon is IBM\u2019s ready made set of serious looking screen pieces, so the tool looks clean without us designing every button.' },
      { li: 'The public website: React with Tailwind and a few animation tools. Tailwind is a quick way to style a site. The animation tools add one calm 3D moment and smooth scrolling, just enough to look modern and trustworthy.' },
      { li: 'The database: PostgreSQL. This is where all the facts live, like the banks, the projects, the files, and the test cases. PostgreSQL is free, solid, and widely used.' },
      { li: 'Hosting: our own server behind Cloudflare. The plan is to run it on our own machine, with Cloudflare in front for safety and speed. This part is planned but not done yet.' },
      'None of these are flashy picks, and that is on purpose. For a bank, boring and reliable beats new and exciting.',
    ],
  },
  {
    title: '9. What is built and working right now',
    blocks: [
      'I tested the whole thing end to end, for real, not just in theory. Here is what actually works today. I am confident about this list because I ran it.',
      { li: 'Signing in, with two kinds of user (an admin and an engineer).' },
      { li: 'Setting up a bank as a client and starting a project for them.' },
      { li: 'Uploading the old code. It figures out the file type, counts the lines, and keeps a fingerprint.' },
      { li: 'The AI explanation, and it is genuinely good. On a real sample I gave it, it correctly explained the program, named the right file and the interest rate, described the logic, and even pointed out that the program had no error handling. That is real understanding, not filler.' },
      { li: 'The dependency map. By reading the code, it correctly found that my main program called another program and used a shared record layout. Real links, found automatically.' },
      { li: 'The modernization flow. I created a piece of work, got an AI Java draft, saved a reviewed version, and approved it. The approval recorded who approved it and when.' },
      { li: 'Verification that really runs both sides. It compiles and runs the original COBOL and the new Java, compares them, and shows a real pass or fail with a diff on a failure. This is the core, and it works now.' },
      { li: 'The backend starts up, connects to the database, applies its database setup steps, and answers a health check.' },
      { li: 'All the automated tests pass. There are 54 of them across the three parts, including new ones that compile and run real COBOL and real Java, and every one passed.' },
      { li: 'Both front ends build with no errors, and the internal tool screens are real and wired to the working backend.' },
      'So the spine of the product is real. You can take a piece of old code and move it all the way through the flow today.',
    ],
  },
  {
    title: '10. What is half done, or still just a shell',
    blocks: [
      'Now the honest other side. Some things still need work.',
      { li: 'The AI Java translation is a draft, not finished code. It is a stronger draft now that we moved to a better model, but a person still reviews and finishes it before it is approved. That human check is the design, not a gap, so plan for an engineer in the loop.' },
      { li: 'The dependency map could be smarter. It correctly finds that one program calls another, but it does not yet connect that mention back to the actual uploaded file for it. So it shows them as two separate dots instead of one.' },
      { li: 'The verification sandbox is process level, not a locked down container. It runs each program in its own process with a hard time limit, which is a safe start, but a setup near a bank should wrap each run in stronger isolation later.' },
      { li: 'The public contact form does not send anywhere yet. It works, but until we point it at a real email service, it just keeps the message in the browser. We have to set that before launch.' },
    ],
  },
  {
    title: '11. What is left to do before this is a real product banks can use',
    blocks: [
      'Here is the to do list, most important first, in plain order. The big one, real verification, is done now, so this list is mostly about getting it ready to run near a real bank.',
      { li: 'Harden the verification sandbox. Today each run is its own process with a time limit, which is safe enough for us. Before a bank, wrap each run in a locked down container with no network, so generated code can never reach anything it should not.' },
      { li: 'Keep improving the translation. The model is strong now, but tidy the drafts a little more so the Java needs even less hand fixing, and add more test inputs so verification covers more.' },
      { li: 'Rotate the AI key. The key we use is sitting in a local settings file and was shared during setup, so it should be treated as exposed and replaced.' },
      { li: 'Set up the real production setup. A real database with a real password (the local one trusts the machine with no password, which is fine on a laptop and not fine in production), real secrets, and the contact form pointed at a real inbox.' },
      { li: 'Actually build and run the whole thing in containers, and make sure GnuCOBOL is in the backend image so verification works there too. The container setup is written but has not been built or run yet.' },
      { li: 'Add deeper automated tests, especially ones that talk to a real database, so we catch more before it reaches a bank.' },
      { li: 'Do the real deploy. Put it on our own server behind Cloudflare, which is planned but not done.' },
      'None of this is a disaster. The foundation is solid, and the core promise, proving the new Java matches the old COBOL, works now. The rest is making it ready for a real bank.',
    ],
  },
  {
    title: '12. How to run the whole thing on a computer',
    blocks: [
      'This is for future me, so I do not have to remember it all. Here is how to run the three parts on a normal machine.',
      { h2: 'What you need first' },
      { li: 'Java 21.' },
      { li: 'Node 22 or newer (this machine runs Node 24).' },
      { li: 'PostgreSQL 14 or newer (this machine runs 18).' },
      { li: 'Git.' },
      { h2: 'The backend (the engine)' },
      { li: 'Open a terminal in the backend folder.' },
      { li: 'Copy application-example.properties to application-local.properties, then fill in the database password and the AI key.' },
      { li: 'Run the backend with the included Maven wrapper. On Windows that is .\\mvnw.cmd spring-boot:run.' },
      { li: 'Check it is alive by opening http://localhost:8080/health. It should say status ok.' },
      { h2: 'The internal tool (our team\u2019s app)' },
      { li: 'Open a terminal in the frontend folder.' },
      { li: 'Run npm install once, then npm run dev.' },
      { li: 'Open the address it prints, usually http://localhost:5173. Run the backend too, so it has something to talk to.' },
      { h2: 'The public website' },
      { li: 'Open a terminal in the website folder.' },
      { li: 'Run npm install once, then npm run dev.' },
      { li: 'Open the address it prints, usually http://localhost:5174.' },
      'That is it. Three folders, three commands each, and the backend needs its local settings file filled in first.',
    ],
  },
  {
    title: '13. A small glossary',
    blocks: [
      'Plain one line meanings for the words a normal person might not know.',
      { li: 'COBOL: a very old programming language that a lot of banks still run their core systems on.' },
      { li: 'Java: a modern, widely used programming language. It is what we rewrite the old COBOL into.' },
      { li: 'Spring Boot: a popular toolkit for building the engine, or backend, of an app in Java.' },
      { li: 'React: a common tool for building the screens of a web app.' },
      { li: 'IBM Carbon: IBM\u2019s ready made set of clean, serious looking screen pieces, used in the internal tool.' },
      { li: 'Tailwind: a quick way to style a website, used on the public site.' },
      { li: 'PostgreSQL: a free, reliable database where we store all the facts.' },
      { li: 'API: the set of doors the backend opens so the screens (and my tests) can ask it to do things.' },
      { li: 'Flyway: the tool that sets up and updates the database tables in a controlled way.' },
      { li: 'JWT: a signed token you get when you sign in, which proves who you are on later requests.' },
      { li: 'Vite: the tool that builds and runs the front end apps while we develop them.' },
      { li: 'Copybook: a shared chunk of COBOL, usually a record layout, that many programs reuse.' },
      { li: 'Dependency map: the picture of how the pieces of code connect, like which program calls which.' },
      { li: 'OpenRouter: the service we go through to reach the rented AI model.' },
    ],
  },
];

// ---- put it all together ----

const body = [...titlePage(), ...contents()];
for (const section of SECTIONS) {
  body.push(h1(section.title));
  body.push(...render(section.blocks));
}

const doc = new Document({
  creator: 'Corewise',
  title: 'The COBOL Modernization Project',
  description: 'A plain, honest report on the project.',
  // Body text default: a clean, readable font at 11pt.
  styles: {
    default: {
      document: { run: { font: 'Calibri', size: 22 } },
    },
  },
  sections: [
    {
      properties: {},
      footers: {
        default: new Footer({
          children: [
            new Paragraph({
              alignment: AlignmentType.CENTER,
              children: [
                new TextRun({ text: 'Corewise project report   |   page ', size: 18, color: '808080' }),
                new TextRun({ children: [PageNumber.CURRENT], size: 18, color: '808080' }),
              ],
            }),
          ],
        }),
      },
      children: body,
    },
  ],
});

const buffer = await Packer.toBuffer(doc);
writeFileSync(OUT, buffer);
console.log('Wrote ' + OUT + ' (' + buffer.length + ' bytes)');
