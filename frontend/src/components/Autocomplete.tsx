import React, { useState, useEffect } from 'react';
import '../styles/Autocomplete.css';

interface AutocompleteProps {
  suggestions: string[];
  // optional controlled value; if omitted the component uses internal input state
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
  const [inputValue, setInputValue] = useState<string>(value ?? '');

  useEffect(() => {
    setInputValue(value ?? '');
  }, [value]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const userInput = e.currentTarget.value;
    setInputValue(userInput);
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
    setInputValue('');
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
