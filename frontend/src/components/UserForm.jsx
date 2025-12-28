import React, { useState } from 'react';
import { userApi } from '../services/api';
import './UserForm.css';

const UserForm = ({ selectedZone }) => {
  const [formData, setFormData] = useState({
    email: '',
    phone: '',
    priceZone: selectedZone || 'NO1',
    alertThreshold: '1.50',
    alertEnabled: true,
  });

  const [status, setStatus] = useState({
    loading: false,
    success: false,
    error: null,
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus({ loading: true, success: false, error: null });

    try {
      const response = await userApi.registerUser(formData);
      console.log('User registered:', response.data);
      
      setStatus({ loading: false, success: true, error: null });
      
      // Reset form
      setFormData({
        email: '',
        phone: '',
        priceZone: selectedZone || 'NO1',
        alertThreshold: '1.50',
        alertEnabled: true,
      });

      // Hide success message after 5 seconds
      setTimeout(() => {
        setStatus((prev) => ({ ...prev, success: false }));
      }, 5000);

    } catch (error) {
      console.error('Registration error:', error);
      const errorMsg = error.response?.data?.error || 'Kunne ikke registrere bruker';
      setStatus({ loading: false, success: false, error: errorMsg });
    }
  };

  return (
    <div className="user-form-container">
      <div className="form-header">
        <h3>🔔 Registrer deg for varsler</h3>
        <p>Få beskjed når strømmen er billig eller dyr</p>
      </div>

      <form onSubmit={handleSubmit} className="user-form">
        <div className="form-group">
          <label htmlFor="email">E-post *</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            placeholder="din@epost.no"
          />
        </div>

        <div className="form-group">
          <label htmlFor="phone">Telefon (valgfritt)</label>
          <input
            type="tel"
            id="phone"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="+47 123 45 678"
          />
        </div>

        <div className="form-group">
          <label htmlFor="priceZone">Prisområde *</label>
          <select
            id="priceZone"
            name="priceZone"
            value={formData.priceZone}
            onChange={handleChange}
            required
          >
            <option value="NO1">NO1 - Oslo / Øst-Norge</option>
            <option value="NO2">NO2 - Kristiansand / Sør-Norge</option>
            <option value="NO3">NO3 - Trondheim / Midt-Norge</option>
            <option value="NO4">NO4 - Tromsø / Nord-Norge</option>
            <option value="NO5">NO5 - Bergen / Vest-Norge</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="alertThreshold">
            Varsle meg når prisen er over/under (kr/kWh)
          </label>
          <input
            type="number"
            id="alertThreshold"
            name="alertThreshold"
            value={formData.alertThreshold}
            onChange={handleChange}
            step="0.10"
            min="0"
            max="10"
            required
          />
          <small>Du får varsel når prisen er under eller over denne grensen</small>
        </div>

        <div className="form-group checkbox-group">
          <label>
            <input
              type="checkbox"
              name="alertEnabled"
              checked={formData.alertEnabled}
              onChange={handleChange}
            />
            <span>Aktiver varsler</span>
          </label>
        </div>

        <button
          type="submit"
          className="submit-btn"
          disabled={status.loading}
        >
          {status.loading ? 'Registrerer...' : '✉️ Registrer meg'}
        </button>

        {status.success && (
          <div className="alert alert-success">
            ✅ Du er nå registrert! Vi sender deg varsler basert på dine preferanser.
          </div>
        )}

        {status.error && (
          <div className="alert alert-error">
            ❌ {status.error}
          </div>
        )}
      </form>
    </div>
  );
};

export default UserForm;