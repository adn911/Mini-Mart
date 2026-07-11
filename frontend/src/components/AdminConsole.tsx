import { clearToken } from "../api";

type AdminConsoleProps = {
  onLogout: () => void;
};

export default function AdminConsole({ onLogout }: AdminConsoleProps) {
  const handleLogout = () => {
    clearToken();
    onLogout();
  };

  return (
    <div className="min-h-screen bg-white text-slate-900">
      <header className="border-b border-slate-200">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <h1 className="text-xl font-semibold tracking-tight">Admin Console</h1>
          <button
            onClick={handleLogout}
            className="border border-slate-200 px-3 py-1.5 text-xs font-medium uppercase tracking-wider text-slate-600 transition-colors hover:border-slate-400"
          >
            Sign out
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-8">
        <p className="text-sm text-slate-500">Select a section to manage.</p>
      </main>
    </div>
  );
}
