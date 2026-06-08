import './App.css'

const quickFacts = [
  {
    label: 'Backend',
    value: 'Spring Boot',
    detail: 'REST API, JPA, validation, and PostgreSQL wiring start here.',
  },
  {
    label: 'Frontend',
    value: 'React + Vite',
    detail: 'A lightweight UI shell with a proxy-ready dev server.',
  },
  {
    label: 'Database',
    value: 'PostgreSQL',
    detail: 'Shared local service for import and browsing workflows.',
  },
]

function App() {
  return (
    <main className="shell">
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Epic 1 baseline</p>
          <h1>Users & Posts Browser</h1>
          <p className="lede">
            The monorepo is initialized with a Spring Boot backend, a React/Vite
            frontend, and PostgreSQL ready for the next epics.
          </p>

          <div className="actions">
            <a className="primary" href="#stack">
              View stack
            </a>
            <a className="secondary" href="#run-guide">
              Run guide
            </a>
          </div>
        </div>

        <aside className="status-card" aria-label="project status">
          <p className="status-label">Current state</p>
          <p className="status-value">Bootstrapped and runnable</p>
          <p className="status-note">
            The app is intentionally minimal for now. Feature work will land in
            the next epics.
          </p>
        </aside>
      </section>

      <section className="grid" id="stack" aria-label="project stack">
        {quickFacts.map((item) => (
          <article className="card" key={item.label}>
            <p className="card-label">{item.label}</p>
            <h2>{item.value}</h2>
            <p>{item.detail}</p>
          </article>
        ))}
      </section>

      <section className="run-guide" id="run-guide">
        <p className="section-label">Minimal run guide</p>
        <ol>
          <li>Copy `.env.example` to `.env` if you want to tweak defaults.</li>
          <li>Run `docker compose up --build` from the repository root.</li>
          <li>Open the frontend at `http://localhost:3000`.</li>
        </ol>
      </section>
    </main>
  )
}

export default App
