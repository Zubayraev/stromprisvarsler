import React, { useState } from 'react';
import Navigation from './components/Navigation';
import HomePage from './pages/HomePage';
import './App.css';

function App() {
  const [selectedZone, setSelectedZone] = useState('NO1');

  const handleZoneChange = (newZone) => {
    setSelectedZone(newZone);
  };

  return (
    <div className="App">
      <Navigation selectedZone={selectedZone} onZoneChange={handleZoneChange} />
      <HomePage selectedZone={selectedZone} />
    </div>
  );
}

export default App;