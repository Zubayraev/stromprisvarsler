import React from 'react';
import './CurrentPrice.css';

const CurrentPrice = ({ priceData, loading, error }) => {
  if (loading) {
    return (
      <div className="current-price loading">
        <div className="spinner"></div>
        <p>Henter pris...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="current-price error">
        <p>⚠️ Kunne ikke hente pris</p>
      </div>
    );
  }

  if (!priceData) {
    return (
      <div className="current-price no-data">
        <p>Ingen prisdata tilgjengelig</p>
      </div>
    );
  }

  // Determine price level and color
  const price = parseFloat(priceData.priceNok);
  let priceLevel = 'medium';
  let emoji = '⚡';

  if (price < 1.0) {
    priceLevel = 'low';
    emoji = '✅';
  } else if (price > 2.0) {
    priceLevel = 'high';
    emoji = '⚠️';
  }

  // Format timestamp
  const timestamp = new Date(priceData.priceTimestamp);
  const timeString = timestamp.toLocaleTimeString('no-NO', {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className={`current-price ${priceLevel}`}>
      <div className="price-header">
        <span className="emoji">{emoji}</span>
        <h2>Strømpris nå</h2>
      </div>
      
      <div className="price-display">
        <span className="price-value">{price.toFixed(2)}</span>
        <span className="price-unit">kr/kWh</span>
      </div>

      <div className="price-info">
        <p className="time">Klokken {timeString}</p>
        <p className="zone">{priceData.zone}</p>
      </div>

      <div className="price-status">
        {priceLevel === 'low' && <p className="status-text">💚 Billig strøm - god tid for vask!</p>}
        {priceLevel === 'medium' && <p className="status-text">💛 Middels pris</p>}
        {priceLevel === 'high' && <p className="status-text">❤️ Høy pris - utsett hvis mulig</p>}
      </div>
    </div>
  );
};

export default CurrentPrice;