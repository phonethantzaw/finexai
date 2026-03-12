import { motion } from 'framer-motion';
import type { ReactNode } from 'react';

interface BlurInProps {
    children: ReactNode;
    duration?: number;
    delay?: number;
    className?: string;
}

export function BlurIn({ children, duration = 0.6, delay = 0, className = '' }: BlurInProps) {
    return (
        <motion.div
            initial={{ opacity: 0, filter: 'blur(10px)', y: 20 }}
            animate={{ opacity: 1, filter: 'blur(0px)', y: 0 }}
            transition={{ duration, delay }}
            className={className}
        >
            {children}
        </motion.div>
    );
}
