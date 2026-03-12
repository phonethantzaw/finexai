import { motion } from 'framer-motion';

interface SplitTextProps {
    text: string;
    delay?: number;
    duration?: number;
    staggerDelay?: number;
    className?: string;
}

export function SplitText({ text, delay = 0, duration = 0.6, staggerDelay = 0.08, className = '' }: SplitTextProps) {
    const words = text.split(' ');

    return (
        <span className={className}>
            {words.map((word, i) => (
                <motion.span
                    key={i}
                    className="inline-block whitespace-pre"
                    initial={{ opacity: 0, y: 40 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration, delay: delay + i * staggerDelay }}
                >
                    {word}{' '}
                </motion.span>
            ))}
        </span>
    );
}
