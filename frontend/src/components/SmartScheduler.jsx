import React, { useState } from 'react';
import './SmartScheduler.css';

const SmartScheduler = ({ todaysPrices }) => {
  const [selectedTask, setSelectedTask] = useState('washing');
  const [duration, setDuration] = useState(1);
  const [results, setResults] = useState(null);

  // Forhåndsdefinerte oppgaver
  const tasks = {
    washing: { name: 'Vaskemaskin', icon: '🧺', defaultDuration: 2 },
    dishwasher: { name: 'Oppvaskmaskin', icon: '🍽️', defaultDuration: 2 },
    ev_charging: { name: 'Elbil lading', icon: '🔌', defaultDuration: 8 },
    heating: { name: 'Oppvarming', icon: '🔥', defaultDuration: 3 },
    drying: { name: 'Tørketrommel', icon: '👕', defaultDuration: 2 },
    pool: { name: 'Bassengpumpe', icon: '🏊', defaultDuration: 4 },
  };

  const findBestTimeSlots = () => {
    if (!todaysPrices || todaysPrices.length === 0) {
      return null;
    }

    const slots = [];
    
    // Finn alle mulige tidsvinduer
    for (let i = 0; i <= todaysPrices.length - duration; i++) {
      const slot = todaysPrices.slice(i, i + duration);
      const avgPrice = slot.reduce((sum, p) => sum + parseFloat(p.priceNok), 0) / duration;
      
      slots.push({
        startIndex: i,
        startTime: new Date(slot[0].priceTimestamp),
        endTime: new Date(slot[slot.length - 1].priceTimestamp),
        prices: slot,
        avgPrice: avgPrice
      });
    }

    // Sorter etter pris
    slots.sort((a, b) => a.avgPrice - b.avgPrice);

    return slots.slice(0, 3); // Returner de 3 beste
  };

  const handleFindBestTime = () => {
    const bestSlots = findBestTimeSlots();
    setResults(bestSlots);
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString('no-NO', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getMedalEmoji = (index) => {
    return ['🥇', '🥈', '🥉'][index] || '⭐';
  };

  return (
    <div className="smart-scheduler glass-card">
      <div className="section-header">
        <h3>🎯 Smart Tidsplanlegger</h3>
        <p className="subtitle">Finn det perfekte tidspunktet for dine aktiviteter</p>
      </div>

      <div className="scheduler-content">
        <div className="input-section">
          <div className="task-grid">
            {Object.entries(tasks).map(([key, task]) => (
              <button
                key={key}
                className={`task-button ${selectedTask === key ? 'active' : ''}`}
                onClick={() => {
                  setSelectedTask(key);
                  setDuration(task.defaultDuration);
                }}
              >
                <span className="task-icon">{task.icon}</span>
                <span className="task-name">{task.name}</span>
              </button>
            ))}
          </div>

          <div className="duration-selector">
            <label htmlFor="duration">Varighet (timer):</label>
            <input
              type="number"
              id="duration"
              value={duration}
              onChange={(e) => setDuration(parseInt(e.target.value))}
              min="1"
              max="12"
              className="duration-input"
            />
          </div>

          <button onClick={handleFindBestTime} className="find-button">
            🔍 Finn beste tidspunkt
          </button>
        </div>

        {results && (
          <div className="results-section">
            <h4>Anbefalte tidspunkter:</h4>
            <div className="time-slots">
              {results.map((slot, index) => (
                <div key={index} className={`time-slot rank-${index + 1}`}>
                  <div className="slot-header">
                    <span className="medal">{getMedalEmoji(index)}</span>
                    <span className="rank-label">
                      {index === 0 ? 'Beste tid' : index === 1 ? 'Andrevalg' : 'Tredjevalg'}
                    </span>
                  </div>
                  <div className="slot-time">
                    {formatTime(slot.startTime)} - {formatTime(new Date(slot.endTime.getTime() + 3600000))}
                  </div>
                  <div className="slot-price">
                    <span className="price-label">Gjennomsnittspris:</span>
                    <span className="price-value">{slot.avgPrice.toFixed(2)} kr/kWh</span>
                  </div>
                  {index === 0 && (
                    <div className="slot-savings">
                      💰 Sparer {(results[2].avgPrice - slot.avgPrice).toFixed(2)} kr/kWh
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="tip-box">
        <p>💡 <strong>Tips:</strong> Start aktiviteten ved beste tidspunkt for maksimal besparelse!</p>
      </div>
    </div>
  );
};

export default SmartScheduler;