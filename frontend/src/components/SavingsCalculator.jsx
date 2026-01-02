// Nytt komponent: SavingsCalculator.jsx
const SavingsCalculator = ({ cheapestPrice, averagePrice }) => {
  const [kwh, setKwh] = useState(5);
  const savings = ((averagePrice - cheapestPrice) * kwh).toFixed(2);

  return (
    <div className="savings-calculator">
      <h3>💰 Besparelseskalkulator</h3>
      <label>
        Forbruk (kWh): 
        <input 
          type="number" 
          value={kwh} 
          onChange={(e) => setKwh(e.target.value)} 
        />
      </label>
      <div className="result">
        Du sparer: <strong>{savings} kr</strong> ved å vente til billigste time
      </div>
    </div>
  );
};