import React, { useState, useEffect } from 'react';
import { priceApi } from '../services/api';
import CurrentPrice from '../components/CurrentPrice';
import PriceChart from '../components/PriceChart';
import CheapestHours from '../components/CheapestHours';
import SmartScheduler from '../components/SmartScheduler';
import SavingsCalculator from '../components/SavingsCalculator';
import UserForm from '../components/UserForm';
import './HomePage.css';

const HomePage = ({ selectedZone }) => {
  const [currentPrice, setCurrentPrice] = useState(null);
  const [todaysPrices, setTodaysPrices] = useState([]);
  const [cheapestHours, setCheapestHours] = useState([]);

  const [loading, setLoading] = useState({
    current: false,
    today: false,
    cheapest: false,
  });

  const [error, setError] = useState({
    current: null,
    today: null,
    cheapest: null,
  });

  // Fetch all data when zone changes
  useEffect(() => {
    fetchAllData();
  }, [selectedZone]);

  const fetchAllData = async () => {
    await Promise.all([
      fetchCurrentPrice(),
      fetchTodaysPrices(),
      fetchCheapestHours(),
    ]);
  };

  const fetchCurrentPrice = async () => {
    setLoading((prev) => ({ ...prev, current: true }));
    setError((prev) => ({ ...prev, current: null }));

    try {
      const response = await priceApi.getCurrentPrice(selectedZone);
      setCurrentPrice(response.data);
    } catch (err) {
      console.error('Error fetching current price:', err);
      setError((prev) => ({ ...prev, current: err.message }));
    } finally {
      setLoading((prev) => ({ ...prev, current: false }));
    }
  };

  const fetchTodaysPrices = async () => {
    setLoading((prev) => ({ ...prev, today: true }));
    setError((prev) => ({ ...prev, today: null }));

    try {
      const response = await priceApi.getTodaysPrices(selectedZone);
      setTodaysPrices(response.data);
    } catch (err) {
      console.error('Error fetching today prices:', err);
      setError((prev) => ({ ...prev, today: err.message }));
    } finally {
      setLoading((prev) => ({ ...prev, today: false }));
    }
  };

  const fetchCheapestHours = async () => {
    setLoading((prev) => ({ ...prev, cheapest: true }));
    setError((prev) => ({ ...prev, cheapest: null }));

    try {
      const response = await priceApi.getCheapestHours(selectedZone, 3);
      setCheapestHours(response.data);
    } catch (err) {
      console.error('Error fetching cheapest hours:', err);
      setError((prev) => ({ ...prev, cheapest: err.message }));
    } finally {
      setLoading((prev) => ({ ...prev, cheapest: false }));
    }
  };

  const handleRefresh = () => {
    fetchAllData();
  };

  return (
    <div className="home-page">
      <div className="container">
        <div className="refresh-section">
          <button onClick={handleRefresh} className="refresh-btn">
            🔄 Oppdater priser
          </button>
        </div>

        <CurrentPrice
          priceData={currentPrice}
          loading={loading.current}
          error={error.current}
        />

        <PriceChart
          pricesData={todaysPrices}
          loading={loading.today}
          error={error.today}
        />

        <CheapestHours
          cheapestData={cheapestHours}
          loading={loading.cheapest}
          error={error.cheapest}
        />

        <SmartScheduler todaysPrices={todaysPrices} />

        <SavingsCalculator todaysPrices={todaysPrices} />

        <UserForm selectedZone={selectedZone} />
      </div>
    </div>
  );
};

export default HomePage;