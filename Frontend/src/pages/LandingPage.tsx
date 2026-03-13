import { motion } from 'framer-motion';
import { Sparkles, ArrowRight } from 'lucide-react';
import { BlurIn } from '@/components/animations/BlurIn';
import { SplitText } from '@/components/animations/SplitText';
export default function LandingPage() {
  return (
    <main className="relative h-screen w-full overflow-hidden bg-[#070612] text-white">
      <div className="absolute inset-0 z-0 ml-[20px] overflow-hidden">
        <video
          src="/Finex-AI/automation.mp4"
          autoPlay
          loop
          muted
          playsInline
          className="h-full w-full scale-[1.2] origin-left object-cover opacity-80"
        />
      </div>

      <div className="absolute bottom-0 left-0 w-full h-40 z-10 bg-gradient-to-t from-[#070612] to-transparent pointer-events-none" />

      <div className="relative z-20 h-full max-w-7xl mx-auto px-6 lg:px-12 flex flex-col justify-center">
        <div className="flex flex-col gap-12">
          <div className="flex flex-col gap-6 max-w-3xl">
            <BlurIn duration={0.6} delay={0}>
              <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-white/20 backdrop-blur-sm self-start">
                <Sparkles className="w-3 h-3 text-white/80" />
                <span className="text-sm font-medium text-white/80">Finex AI</span>
              </div>
            </BlurIn>

            <h1 className="text-4xl md:text-5xl lg:text-6xl text-white font-medium leading-tight lg:leading-[1.2]">
              <span className="block">
                <SplitText text="Financial Intelligence" delay={0.1} />
              </span>
              <span className="inline">
                <SplitText text="Powered by" delay={0.6} />{' '}
              </span>
              <span className="inline font-serif italic text-white/90">
                <SplitText text="AI." delay={0.8} />
              </span>
            </h1>

            <BlurIn duration={0.6} delay={0.4}>
              <p className="text-white/80 text-lg font-normal leading-relaxed max-w-xl">
              Analyze financial documents with AI and get instant insights through natural conversation.
              </p>
            </BlurIn>
          </div>

          <BlurIn duration={0.6} delay={0.6}>
            <div className="flex flex-wrap gap-4 items-center">
              <motion.a
                href="/login"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.97 }}
                transition={{ type: 'spring', stiffness: 400, damping: 20 }}
                className="inline-flex items-center justify-center bg-white text-[#070612] rounded-full px-5 py-3 font-medium text-sm gap-2"
              >
                Try Finex
                <motion.span
                  animate={{ x: [0, 4, 0] }}
                  transition={{ repeat: Infinity, duration: 1.4, ease: 'easeInOut' }}
                >
                  <ArrowRight className="w-4 h-4" />
                </motion.span>
              </motion.a>
            </div>
          </BlurIn>
        </div>
      </div>
    </main>
  );
}
