import React, { useState } from 'react';
import '../styles/Autocomplete.css';

interface AutocompleteProps {
  suggestions: string[];
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onSelect: (value: string) => void;
  onKeyDown?: (e: React.KeyboardEvent<HTMLInputElement>) => void;
  name: string;
  id: string;
  placeholder: string;
}

export const Autocomplete: React.FC<AutocompleteProps> = ({ suggestions, value, onChange, onSelect, onKeyDown, name, id, placeholder }) => {
  const [filteredSuggestions, setFilteredSuggestions] = useState<string[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [internalValue, setInternalValue] = useState('');
  const isControlled = value !== undefined;
  const inputValue = isControlled ? value : internalValue;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const userInput = e.currentTarget.value;
    if (!isControlled) {
      setInternalValue(userInput);
    }
    const filtered = suggestions.filter(
      suggestion => suggestion.toLowerCase().indexOf(userInput.toLowerCase()) > -1
    );
    setFilteredSuggestions(filtered);
    setShowSuggestions(true);
    if (onChange) onChange(e);
  };

  const handleClick = (suggestion: string) => {
    onSelect(suggestion);
    setShowSuggestions(false);
    if (!isControlled) {
      setInternalValue('');
    }
  };

  const suggestionsListComponent = () => {
    if (showSuggestions && inputValue) {
      if (filteredSuggestions.length) {
        return (
          <ul className="autocomplete-suggestions">
            {filteredSuggestions.map((suggestion, index) => {
              return (
                <li key={index} onMouseDown={() => handleClick(suggestion)} className="autocomplete-suggestion">
                  {suggestion}
                </li>
              );
            })}
          </ul>
        );
      } else {
        return (
          <div className="autocomplete-no-suggestions">
            <em>No suggestions available.</em>
          </div>
        );
      }
    }
    return null;
  };

  return (
    <div className="autocomplete-container">
      <input
        type="text"
        onChange={handleChange}
        value={inputValue}
        onKeyDown={onKeyDown}
        name={name}
        id={id}
        placeholder={placeholder}
        className="autocomplete-input"
        onBlur={() => setTimeout(() => setShowSuggestions(false), 100)}
      />
      {suggestionsListComponent()}
    </div>
  );
};
