import React from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Typography,
  CircularProgress,
  Box,
  Chip,
  IconButton,
  Tooltip,
  TextField,
  InputAdornment
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';

const AnimalTable = ({ animals, loading, selectedAnimal, onSelectAnimal, onPageChange, onEdit, onDelete, isAdmin }) => {
  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(10);
  const [orderBy, setOrderBy] = React.useState('name');
  const [order, setOrder] = React.useState('asc');
  const [searchQuery, setSearchQuery] = React.useState('');

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleRowClick = (animal) => {
    onSelectAnimal(animal);
  };

  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const sortedAnimals = React.useMemo(() => {
    if (!animals) return [];
    return [...animals].sort((a, b) => {
      let aValue = a[orderBy];
      let bValue = b[orderBy];
      
      // Handle null/undefined values
      if (aValue == null) return 1;
      if (bValue == null) return -1;
      
      // Convert to lowercase for string comparison
      if (typeof aValue === 'string') aValue = aValue.toLowerCase();
      if (typeof bValue === 'string') bValue = bValue.toLowerCase();
      
      if (order === 'asc') {
        return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      } else {
        return bValue < aValue ? -1 : bValue > aValue ? 1 : 0;
      }
    });
  }, [animals, orderBy, order]);

  // Filter animals based on search query
  const filteredAnimals = React.useMemo(() => {
    if (!searchQuery.trim()) return sortedAnimals;
    
    const query = searchQuery.toLowerCase();
    return sortedAnimals.filter(animal => 
      animal.name?.toLowerCase().includes(query)
    );
  }, [sortedAnimals, searchQuery]);

  const displayedAnimals = filteredAnimals.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  // Notify parent component when displayed animals change
  React.useEffect(() => {
    if (onPageChange) {
      onPageChange(displayedAnimals);
    }
  }, [page, rowsPerPage, filteredAnimals.length]); // Only re-run when pagination or filter count changes

  // Reset to first page when search query changes
  React.useEffect(() => {
    setPage(0);
  }, [searchQuery]);

  if (loading) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <CircularProgress />
        <Typography sx={{ mt: 2 }}>Loading animals...</Typography>
      </Paper>
    );
  }

  if (!animals || animals.length === 0) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography>No animals found</Typography>
      </Paper>
    );
  }

  return (
    <Paper elevation={2}>
      <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h6">
          Animal Records ({filteredAnimals.length} {searchQuery ? 'found' : 'total'})
        </Typography>
        <TextField
          size="small"
          placeholder="Search by name..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          sx={{ width: 300 }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
      </Box>
      <TableContainer sx={{ maxHeight: 500 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'name'}
                  direction={orderBy === 'name' ? order : 'asc'}
                  onClick={() => handleRequestSort('name')}
                >
                  <strong>Name</strong>
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'breed'}
                  direction={orderBy === 'breed' ? order : 'asc'}
                  onClick={() => handleRequestSort('breed')}
                >
                  <strong>Breed</strong>
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'age_upon_outcome'}
                  direction={orderBy === 'age_upon_outcome' ? order : 'asc'}
                  onClick={() => handleRequestSort('age_upon_outcome')}
                >
                  <strong>Age</strong>
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'sex_upon_outcome'}
                  direction={orderBy === 'sex_upon_outcome' ? order : 'asc'}
                  onClick={() => handleRequestSort('sex_upon_outcome')}
                >
                  <strong>Sex</strong>
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'color'}
                  direction={orderBy === 'color' ? order : 'asc'}
                  onClick={() => handleRequestSort('color')}
                >
                  <strong>Color</strong>
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'animal_type'}
                  direction={orderBy === 'animal_type' ? order : 'asc'}
                  onClick={() => handleRequestSort('animal_type')}
                >
                  <strong>Type</strong>
                </TableSortLabel>
              </TableCell>
              {isAdmin && (
                <TableCell align="center">
                  <strong>Actions</strong>
                </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {displayedAnimals.map((animal, index) => (
              <TableRow
                key={animal._id || index}
                hover
                onClick={() => handleRowClick(animal)}
                sx={{
                  cursor: 'pointer',
                  backgroundColor:
                    selectedAnimal?._id === animal._id ? '#e3f2fd' : 'inherit'
                }}
              >
                <TableCell>{animal.name || 'Unnamed'}</TableCell>
                <TableCell>{animal.breed}</TableCell>
                <TableCell>{animal.age_upon_outcome}</TableCell>
                <TableCell>
                  <Chip
                    label={animal.sex_upon_outcome}
                    size="small"
                    color={
                      animal.sex_upon_outcome?.includes('Male')
                        ? 'primary'
                        : 'secondary'
                    }
                  />
                </TableCell>
                <TableCell>{animal.color}</TableCell>
                <TableCell>
                  <Chip
                    label={animal.animal_type}
                    size="small"
                    variant="outlined"
                  />
                </TableCell>
                {isAdmin && (
                  <TableCell align="center">
                    <Tooltip title="Edit">
                      <IconButton
                        size="small"
                        color="primary"
                        onClick={(e) => {
                          e.stopPropagation();
                          onEdit(animal);
                        }}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton
                        size="small"
                        color="error"
                        onClick={(e) => {
                          e.stopPropagation();
                          onDelete(animal);
                        }}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={filteredAnimals.length}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[5, 10, 25, 50, 100]}
      />
    </Paper>
  );
};

export default AnimalTable;
