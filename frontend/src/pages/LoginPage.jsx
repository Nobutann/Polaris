import { useState } from 'react';
import { login } from '../api';

export function LoginPage({ onLoginSuccess }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        setLoading(true);
        
        try {
            const data = await login(email, password);
            localStorage.setItem('jwt_token', data.token);
            localStorage.setItem('jwt_role', data.role);
            onLoginSuccess(data.role);
        } catch (error) {
            setError(error.message || 'Credenciais inválidas');
        } finally {
            setLoading(false);
        }
    }

    return (
        <main className="min-h-screen flex items-center justify-center bg-[#f3f4f6]">
            <section className="bg-white border border-[#e5e7eb] rounded-xl shadow-sm p-10 w-full max-w-md">
                <header className="mb-8">
                    <h1 className="flex items-center gap-2 mb-1">
                        <span className="text-[#1852b4] font-black text-2xl italic tracking-tight">Polaris</span>
                        <span className="text-xs font-semibold text-white bg-[#04255d] px-2 py-0.5 rounded">SEBRAE</span>
                    </h1>
                    <p className="text-sm text-[#6b7280]">Plataforma de Analytics e Social Listening</p>
                </header>
        
                <form onSubmit={handleSubmit} className="flex flex-col gap-5" noValidate>
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="email" className="text-xs font-semibold text-[#6b7280] uppercase tracking-wide">
                            E-mail
                        </label>
                        <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        placeholder="seu@email.com"
                        required
                        autoFocus
                        autoComplete="email"
                        className="border border-[#e5e7eb] rounded-lg px-4 py-2.5 text-sm text-[#1f2937] outline-none focus:border-[#1852b4] focus:ring-2 focus:ring-[#1852b4]/10 transition"
                        />
                    </div>
        
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="password" className="text-xs font-semibold text-[#6b7280] uppercase tracking-wide">
                        Senha
                        </label>
                        <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                        autoComplete="current-password"
                        className="border border-[#e5e7eb] rounded-lg px-4 py-2.5 text-sm text-[#1f2937] outline-none focus:border-[#1852b4] focus:ring-2 focus:ring-[#1852b4]/10 transition"
                        />
                    </div>
        
                    {error && (
                        <p role="alert" className="text-sm text-[#b91c1c] bg-[#fee2e2] rounded-lg px-4 py-2.5">
                        {error}
                        </p>
                    )}
        
                    <button
                        type="submit"
                        disabled={loading}
                        aria-busy={loading}
                        className="bg-[#04255d] hover:bg-[#1852b4] text-white font-semibold rounded-lg py-2.5 text-sm transition disabled:opacity-60 disabled:cursor-not-allowed mt-1"
                    >
                        {loading ? 'Entrando...' : 'Entrar'}
                    </button>
                </form>
            </section>
    </main>
  );
}