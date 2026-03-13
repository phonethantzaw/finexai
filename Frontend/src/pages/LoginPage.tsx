import { SignIn } from '@clerk/react';
export default function LoginPage() {
  return (
    <main className="relative min-h-screen w-full overflow-hidden bg-[#070612] text-white">
      <div className="absolute inset-0 z-0 overflow-hidden">
        <video
          src="/Finex-AI/automation.mp4"
          autoPlay loop muted playsInline
          className="h-full w-full object-cover opacity-60"
        />
      </div>
      <div className="absolute inset-0 z-10 bg-[#070612]/50" />

      <div className="relative z-20 min-h-screen flex items-center justify-center px-4">
        <SignIn routing="hash" fallbackRedirectUrl="/app/home" />
      </div>
    </main>
  );
}
