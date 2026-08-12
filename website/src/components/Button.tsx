import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'text' | 'danger';
  fullWidth?: boolean;
}

const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  fullWidth = false,
  className = '',
  ...props
}) => {
  const baseStyles = 'px-6 py-3 rounded-xl font-bold transition-all flex items-center justify-center text-sm';
  const variants = {
    primary: 'bg-primary text-white shadow-lg shadow-primary/20 hover:bg-primary-variant',
    secondary: 'bg-secondary text-white shadow-lg shadow-secondary/20 hover:opacity-90',
    outline: 'border-2 border-primary text-primary hover:bg-primary/5',
    text: 'text-primary hover:bg-primary/5',
    danger: 'bg-red-50 text-red-500 hover:bg-red-100'
  };

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${fullWidth ? 'w-full' : ''} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
