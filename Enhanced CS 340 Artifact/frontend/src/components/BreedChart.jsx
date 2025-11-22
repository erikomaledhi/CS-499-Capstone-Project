import React, { useState, useEffect } from 'react';
import { Paper, Typography, Box } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import animalService from '../services/animalService';

const COLORS = [
  '#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8',
  '#82CA9D', '#FFC658', '#FF6B9D', '#9C27B0', '#4CAF50'
];

const BreedChart = () => {
  const [chartData, setChartData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalAnimals, setTotalAnimals] = useState(0);

  useEffect(() => {
    const fetchBreedCounts = async () => {
      try {
        setLoading(true);
        const response = await animalService.getBreedCounts(10);
        const breeds = response.data.breeds;
        const total = response.data.totalAnimals;

        const data = breeds.map(item => ({
          name: item.breed.length > 25 ? item.breed.substring(0, 22) + '...' : item.breed,
          fullName: item.breed,
          value: item.count,
          percentage: ((item.count / total) * 100).toFixed(1)
        }));

        setChartData(data);
        setTotalAnimals(total);
      } catch (error) {
        console.error('Error fetching breed counts:', error);
        setChartData([]);
      } finally {
        setLoading(false);
      }
    };

    fetchBreedCounts();
  }, []);

  if (loading) {
    return (
      <Paper elevation={2} sx={{ p: 3, height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography color="text.secondary">Loading breed distribution...</Typography>
      </Paper>
    );
  }

  if (!chartData || chartData.length === 0) {
    return (
      <Paper elevation={2} sx={{ p: 3, height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography color="text.secondary">No data available for chart</Typography>
      </Paper>
    );
  }

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <Paper sx={{ p: 1.5, border: '1px solid #ccc' }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {payload[0].payload.fullName}
          </Typography>
          <Typography variant="body2" color="primary">
            Count: {payload[0].value}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {payload[0].payload.percentage}%
          </Typography>
        </Paper>
      );
    }
    return null;
  };

  return (
    <Paper elevation={2} sx={{ p: 3, height: 400 }}>
      <Typography variant="h6" gutterBottom>
        Top 10 Breed Distribution
      </Typography>
      <ResponsiveContainer width="100%" height="90%">
        <BarChart 
          data={chartData}
          margin={{ top: 5, right: 30, left: 20, bottom: 80 }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis 
            dataKey="name" 
            angle={-45}
            textAnchor="end"
            height={100}
            interval={0}
            style={{ fontSize: '11px' }}
          />
          <YAxis 
            label={{ value: 'Number of Animals', angle: -90, position: 'insideLeft' }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="value" radius={[8, 8, 0, 0]}>
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </Paper>
  );
};

export default BreedChart;
