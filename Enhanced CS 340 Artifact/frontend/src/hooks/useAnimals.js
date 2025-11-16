import { useState, useEffect, useCallback } from 'react';
import animalService from '../services/animalService';

/**
 * Custom hook to fetch and manage animals data
 */
const useAnimals = (filterType = 'all', params = {}) => {
  const [animals, setAnimals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const refetch = useCallback(() => {
    setRefreshTrigger(prev => prev + 1);
  }, []);

  useEffect(() => {
    const fetchAnimals = async () => {
      setLoading(true);
      setError(null);

      try {
        let response;

        switch (filterType) {
          case 'water':
            response = await animalService.getWaterRescueDogs();
            setAnimals(response.data.animals);
            setPagination(null);
            break;
          case 'mountain':
            response = await animalService.getMountainRescueDogs();
            setAnimals(response.data.animals);
            setPagination(null);
            break;
          case 'disaster':
            response = await animalService.getDisasterRescueDogs();
            setAnimals(response.data.animals);
            setPagination(null);
            break;
          default:
            response = await animalService.getAnimals(params);
            setAnimals(response.data.animals);
            setPagination(response.data.pagination);
        }
      } catch (err) {
        setError(err.message);
        setAnimals([]);
      } finally {
        setLoading(false);
      }
    };

    fetchAnimals();
  }, [filterType, JSON.stringify(params), refreshTrigger]);

  return { animals, loading, error, pagination, refetch };
};

export default useAnimals;
