import { useRef } from 'react';

export function useFilePicker() {
  const inputRef = useRef(null);
  const openPicker = () => inputRef.current?.click();
  const resetPicker = () => { if (inputRef.current) inputRef.current.value = ''; };
  return { inputRef, openPicker, resetPicker };
}
