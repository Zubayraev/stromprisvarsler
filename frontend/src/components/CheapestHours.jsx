import React from 'react';
import './CheapestHours.css';

const CheapestHours = ({ cheapestData, loading, error }) => {
  if (loading) {
    return (
      <div className="cheapest-hours loading">
        <p>Henter billigste timer...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="cheapest-hours error">
        <p>⚠️ Kunne ikke hente data</p>
      </div>
    );
  }

  if (!cheapestData || cheapestData.length === 0) {
    return (
      <div className="cheapest-hours no-data">
        <p>Ingen data tilgjengelig</p>
      </div>
    );
  }

  return (
    <div className="cheapest-hours">
      <div className="section-header">
        <h3>💡 De 3 billigste timene i dag</h3>
        <p className="subtitle">Perfekt tid for strømkrevende oppgaver</p>
      </div>

      <div className="hours-grid">
        {cheapestData.map((price, index) => {
          const timestamp = new Date(price.priceTimestamp);
          const hour = timestamp.getHours();
          const nextHour = (hour + 1) % 24;
          const priceValue = parseFloat(price.priceNok);

          return (
            <div key={index} className="hour-card">
              <div className="card-rank">#{index + 1}</div>
              <div className="card-time">
                {hour.toString().padStart(2, '0')}:00 - {nextHour.toString().padStart(2, '0')}:00
              </div>
              <div className="card-price">
                <span className="price-value">{priceValue.toFixed(2)}</span>
                <span className="price-unit">kr/kWh</span>
              </div>
              <div className="card-emoji">
                {index === 0 ? '🏆' : index === 1 ? '🥈' : '🥉'}
              </div>
            </div>
          );
        })}
      </div>

      <div className="tip-box">
        <p>💡 <strong>Tips:</strong> Planlegg vask, oppvaskmaskin og elbil-lading til disse timene for å spare penger!</p>
      </div>
    </div>
  );
};

export default CheapestHours;