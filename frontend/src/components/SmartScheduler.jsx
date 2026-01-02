// SmartScheduler.jsx
const SmartScheduler = ({ todaysPrices }) => {
  const [task, setTask] = useState('');
  const [duration, setDuration] = useState(1);
  
  const findBestTimeSlot = () => {
    // Finn beste sammenhengende periode
    let bestSlot = null;
    let lowestCost = Infinity;
    
    for (let i = 0; i <= todaysPrices.length - duration; i++) {
      const slot = todaysPrices.slice(i, i + duration);
      const avgPrice = slot.reduce((sum, p) => sum + p.priceNok, 0) / duration;
      
      if (avgPrice < lowestCost) {
        lowestCost = avgPrice;
        bestSlot = slot;
      }
    }
    
    return bestSlot;
  };

  return (
    <div className="smart-scheduler">
      <h3>🎯 Smart tidsplanlegger</h3>
      <input 
        placeholder="Hva skal du gjøre? (vask, lading, etc.)"
        value={task}
        onChange={(e) => setTask(e.target.value)}
      />
      <select value={duration} onChange={(e) => setDuration(Number(e.target.value))}>
        <option value={1}>1 time</option>
        <option value={2}>2 timer</option>
        <option value={3}>3 timer</option>
      </select>
      <button onClick={findBestTimeSlot}>Finn beste tid</button>
    </div>
  );
};

const CommunityStats = () => {
  return (
    <div className="community-stats">
      <h3>👥 Fellesskapsstatistikk</h3>
      <div className="stat-item">
        <span>Gjennomsnittlig besparelse:</span>
        <strong>245 kr/mnd</strong>
      </div>
      <div className="stat-item">
        <span>Mest populære vasketid:</span>
        <strong>23:00 - 01:00</strong>
      </div>
    </div>
  );
};