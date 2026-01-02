import React, { useState } from 'react';
import './SavingsCalculator.css';

const SavingsCalculator = ({ todaysPrices }) => {
  const [selectedAppliance, setSelectedAppliance] = useState('custom');
  const [customKwh, setCustomKwh] = useState(5);

  // Forhåndsdefinerte apparater med typisk forbruk
  const appliances = {
    washing: { name: 'Vaskemaskin', kwh: 1.5 },
    dishwasher: { name: 'Oppvaskmaskin', kwh: 1.2 },
    dryer: { name: 'Tørketrommel', kwh: 3.5 },
    ev: { name: 'Elbil (full lading)', kwh: 50 },
    heater: { name: 'Varmepumpe (1 time)', kwh: 2.5 },
    custom: { name: 'Egendefinert', kwh: customKwh }
  };

  // Beregn priser
  const getKwh = () => {
    if (selectedAppliance === 'custom') {
      return parseFloat(customKwh);
    }
    return appliances[selectedAppliance].kwh;
  };

  const calculatePrices = () => {
    if (!todaysPrices || todaysPrices.length === 0) {
      return { best: 0, average: 0, worst: 0, savings: 0 };
    }

    const kwh = getKwh();
    const prices = todaysPrices.map(p => parseFloat(p.priceNok));
    
    const bestPrice = Math.min(...prices);
    const worstPrice = Math.max(...prices);
    const averagePrice = prices.reduce((a, b) => a + b, 0) / prices.length;

    return {
      best: (bestPrice * kwh).toFixed(2),
      average: (averagePrice * kwh).toFixed(2),
      worst: (worstPrice * kwh).toFixed(2),
      savings: ((averagePrice - bestPrice) * kwh).toFixed(2),
      savingsVsWorst: ((worstPrice - bestPrice) * kwh).toFixed(2)
    };
  };

  const prices = calculatePrices();
  const monthlySavings = (prices.savings * 30).toFixed(0);
  const yearlySavings = (prices.savings * 365).toFixed(0);

  return (
    <div className="savings-calculator glass-card">
      <div className="section-header">
        <h3>💰 Besparelseskalkulator</h3>
        <p className="subtitle">Se hvor mye du sparer ved å velge riktig tidspunkt</p>
      </div>

      <div className="calculator-content">
        <div className="input-section">
          <label htmlFor="appliance-select">Velg apparat eller aktivitet:</label>
          <select
            id="appliance-select"
            value={selectedAppliance}
            onChange={(e) => setSelectedAppliance(e.target.value)}
            className="appliance-select"
          >
            {Object.entries(appliances).map(([key, app]) => (
              <option key={key} value={key}>
                {app.name} {key !== 'custom' && `(${app.kwh} kWh)`}
              </option>
            ))}
          </select>

          {selectedAppliance === 'custom' && (
            <div className="custom-input">
              <label htmlFor="kwh-input">Forbruk (kWh):</label>
              <input
                type="number"
                id="kwh-input"
                value={customKwh}
                onChange={(e) => setCustomKwh(e.target.value)}
                min="0.1"
                step="0.1"
                className="kwh-input"
              />
            </div>
          )}
        </div>

        <div className="results-section">
          <div className="price-comparison">
            <div className="price-box best">
              <div className="box-label">Beste tid</div>
              <div className="box-value">{prices.best} kr</div>
            </div>
            <div className="price-box average">
              <div className="box-label">Gjennomsnitt</div>
              <div className="box-value">{prices.average} kr</div>
            </div>
            <div className="price-box worst">
              <div className="box-label">Verste tid</div>
              <div className="box-value">{prices.worst} kr</div>
            </div>
          </div>

          <div className="savings-highlight">
            <div className="savings-main">
              <span className="savings-label">Du sparer per bruk:</span>
              <span className="savings-amount">{prices.savings} kr</span>
            </div>
            <div className="savings-extra">
              <span className="savings-label-small">Maksimal besparelse:</span>
              <span className="savings-amount-small">{prices.savingsVsWorst} kr</span>
            </div>
          </div>

          <div className="projections">
            <div className="projection">
              <span className="projection-label">📅 Per måned:</span>
              <span className="projection-value">{monthlySavings} kr</span>
            </div>
            <div className="projection">
              <span className="projection-label">📆 Per år:</span>
              <span className="projection-value highlight">{yearlySavings} kr</span>
            </div>
          </div>
        </div>
      </div>

      <div className="tip-box">
        <p>💡 <strong>Tips:</strong> Bruk billigste time konsekvent for å maksimere besparelsene dine!</p>
      </div>
    </div>
  );
};

export default SavingsCalculator;