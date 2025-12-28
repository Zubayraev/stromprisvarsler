import React from 'react';
import './Navigation.css';

const Navigation = ({ selectedZone, onZoneChange }) => {
  const zones = [
    { code: 'NO1', name: 'Oslo / Øst-Norge' },
    { code: 'NO2', name: 'Kristiansand / Sør-Norge' },
    { code: 'NO3', name: 'Trondheim / Midt-Norge' },
    { code: 'NO4', name: 'Tromsø / Nord-Norge' },
    { code: 'NO5', name: 'Bergen / Vest-Norge' },
  ];

  return (
    <nav className="navigation">
      <div className="nav-container">
        <div className="nav-brand">
          <h1>⚡ Strømpris-Varsler</h1>
          <p className="tagline">Smart strømforbruk i Norge</p>
        </div>

        <div className="nav-zone-selector">
          <label htmlFor="zone-select">Velg område:</label>
          <select
            id="zone-select"
            value={selectedZone}
            onChange={(e) => onZoneChange(e.target.value)}
            className="zone-select"
          >
            {zones.map((zone) => (
              <option key={zone.code} value={zone.code}>
                {zone.code} - {zone.name}
              </option>
            ))}
          </select>
        </div>
      </div>
    </nav>
  );
};

export default Navigation;