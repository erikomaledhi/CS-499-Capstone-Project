import React, { useEffect, useRef } from 'react';
import { Paper, Typography } from '@mui/material';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default marker icons in React
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
});

const LocationMap = ({ animals, selectedAnimal }) => {
  const mapRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markersRef = useRef([]);

  useEffect(() => {
    // Initialize map
    if (!mapInstanceRef.current && mapRef.current) {
      mapInstanceRef.current = L.map(mapRef.current).setView([30.75, -97.48], 10);

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 18
      }).addTo(mapInstanceRef.current);
    }

    return () => {
      // Cleanup on unmount
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (!mapInstanceRef.current || !animals || animals.length === 0) return;

    // Clear existing markers
    markersRef.current.forEach((marker) => marker.remove());
    markersRef.current = [];

    // Add markers for all animals
    const bounds = [];
    animals.forEach((animal) => {
      if (animal.location_lat && animal.location_long) {
        const lat = parseFloat(animal.location_lat);
        const lng = parseFloat(animal.location_long);

        if (!isNaN(lat) && !isNaN(lng)) {
          const isSelected = selectedAnimal?._id === animal._id;

          const icon = L.icon({
            iconUrl: isSelected
              ? 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png'
              : 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          });

          const marker = L.marker([lat, lng], { icon })
            .addTo(mapInstanceRef.current)
            .bindPopup(
              `<strong>${animal.name || 'Unnamed'}</strong><br/>
               Breed: ${animal.breed}<br/>
               Age: ${animal.age_upon_outcome}<br/>
               Sex: ${animal.sex_upon_outcome}`
            );

          if (isSelected) {
            marker.openPopup();
          }

          markersRef.current.push(marker);
          bounds.push([lat, lng]);
        }
      }
    });

    // Fit map to show all markers
    if (bounds.length > 0) {
      mapInstanceRef.current.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [animals, selectedAnimal]);

  useEffect(() => {
    // Center map on selected animal
    if (selectedAnimal && selectedAnimal.location_lat && selectedAnimal.location_long) {
      const lat = parseFloat(selectedAnimal.location_lat);
      const lng = parseFloat(selectedAnimal.location_long);

      if (!isNaN(lat) && !isNaN(lng)) {
        mapInstanceRef.current.setView([lat, lng], 13);
      }
    }
  }, [selectedAnimal]);

  return (
    <Paper elevation={2} sx={{ p: 3, height: 400 }}>
      <Typography variant="h6" gutterBottom>
        Geolocation Map
      </Typography>
      <div
        ref={mapRef}
        style={{
          height: 'calc(100% - 40px)',
          width: '100%',
          borderRadius: '4px'
        }}
      />
    </Paper>
  );
};

export default LocationMap;
