import React from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';
import './PriceChart.css';

const PriceChart = ({ pricesData, loading, error }) => {
  if (loading) {
    return (
      <div className="price-chart loading">
        <p>Henter prisdata...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="price-chart error">
        <p>⚠️ Kunne ikke hente prisdata</p>
      </div>
    );
  }

  if (!pricesData || pricesData.length === 0) {
    return (
      <div className="price-chart no-data">
        <p>Ingen prisdata tilgjengelig for i dag</p>
      </div>
    );
  }

  // Format data for recharts
  const chartData = pricesData.map((price) => {
    const timestamp = new Date(price.priceTimestamp);
    const hour = timestamp.getHours();
    
    return {
      time: `${hour.toString().padStart(2, '0')}:00`,
      price: parseFloat(price.priceNok),
      fullTime: timestamp.toLocaleTimeString('no-NO', {
        hour: '2-digit',
        minute: '2-digit',
      }),
    };
  });

  // Find min and max for Y-axis
  const prices = chartData.map((d) => d.price);
  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);
  const avgPrice = prices.reduce((a, b) => a + b, 0) / prices.length;

  // Custom tooltip
  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="custom-tooltip">
          <p className="time">{data.fullTime}</p>
          <p className="price">{data.price.toFixed(2)} kr/kWh</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="price-chart">
      <div className="chart-header">
        <h3>📈 Dagens strømpriser</h3>
        <div className="chart-stats">
          <span className="stat">
            <span className="label">Snitt:</span>
            <span className="value">{avgPrice.toFixed(2)} kr/kWh</span>
          </span>
          <span className="stat">
            <span className="label">Min:</span>
            <span className="value green">{minPrice.toFixed(2)} kr/kWh</span>
          </span>
          <span className="stat">
            <span className="label">Maks:</span>
            <span className="value red">{maxPrice.toFixed(2)} kr/kWh</span>
          </span>
        </div>
      </div>

      <ResponsiveContainer width="100%" height={400}>
        <AreaChart data={chartData}>
          <defs>
            <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#3498db" stopOpacity={0.8} />
              <stop offset="95%" stopColor="#3498db" stopOpacity={0.1} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
          <XAxis
            dataKey="time"
            stroke="#666"
            tick={{ fontSize: 12 }}
            interval={2}
          />
          <YAxis
            stroke="#666"
            tick={{ fontSize: 12 }}
            domain={[Math.floor(minPrice * 0.9), Math.ceil(maxPrice * 1.1)]}
            label={{
              value: 'kr/kWh',
              angle: -90,
              position: 'insideLeft',
              style: { fontSize: 14 },
            }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Area
            type="monotone"
            dataKey="price"
            stroke="#3498db"
            strokeWidth={3}
            fill="url(#colorPrice)"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
};

export default PriceChart;