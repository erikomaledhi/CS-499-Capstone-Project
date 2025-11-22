

class BSTNode {
  /**
   * Create a BST node
   * @param {Object} animal - Animal document from MongoDB
   */
  constructor(animal) {
    this.animal = animal;
    this.animalId = animal.animal_id; // Key for BST organization
    this.left = null;
    this.right = null;
  }
}

class AnimalBST {
  constructor() {
    this.root = null;
    this.size = 0;
    this.buildTime = 0;
    this.totalComparisons = 0; // Track comparisons for analysis
  }

  /**
   * Insert an animal into the BST
   * @param {Object} animal - Animal document
   * @returns {boolean} - True if inserted, false if duplicate
   * 
   * Time Complexity: O(log n) average case, O(n) worst case
   */
  insert(animal) {
    if (!animal || !animal.animal_id) {
      throw new Error('Animal must have an animal_id property');
    }

    const newNode = new BSTNode(animal);
    
    if (!this.root) {
      this.root = newNode;
      this.size++;
      return true;
    }

    return this._insertRecursive(this.root, newNode);
  }

  /**
   * Recursive helper for insertion
   * @private
   */
  _insertRecursive(node, newNode) {
    this.totalComparisons++;

    if (newNode.animalId === node.animalId) {
      // Duplicate - update existing node with latest data
      node.animal = newNode.animal;
      return false;
    }

    if (newNode.animalId < node.animalId) {
      // Go left
      if (!node.left) {
        node.left = newNode;
        this.size++;
        return true;
      }
      return this._insertRecursive(node.left, newNode);
    } else {
      // Go right
      if (!node.right) {
        node.right = newNode;
        this.size++;
        return true;
      }
      return this._insertRecursive(node.right, newNode);
    }
  }

  /**
   * Search for an animal by ID
   * @param {string} animalId - The animal_id to search for
   * @returns {Object|null} - Animal object or null if not found
   * 
   * Time Complexity: O(log n) average case, O(n) worst case
   */
  search(animalId) {
    if (!animalId) {
      return null;
    }

    const comparisons = { count: 0 };
    const result = this._searchRecursive(this.root, animalId, comparisons);
    
    return result ? { animal: result.animal, comparisons: comparisons.count } : null;
  }

  /**
   * Recursive helper for search
   * @private
   */
  _searchRecursive(node, animalId, comparisons) {
    if (!node) {
      return null;
    }

    comparisons.count++;

    if (animalId === node.animalId) {
      return node;
    }

    if (animalId < node.animalId) {
      return this._searchRecursive(node.left, animalId, comparisons);
    } else {
      return this._searchRecursive(node.right, animalId, comparisons);
    }
  }

  /**
   * Build BST from array of animals
   * @param {Array} animals - Array of animal documents
   * @returns {Object} - Build statistics
   * 
   */
  buildFromArray(animals) {
    const startTime = Date.now();
    this.clear();

    if (!animals || animals.length === 0) {
      return {
        size: 0,
        buildTime: 0,
        averageDepth: 0
      };
    }

    // Shuffle array to improve balance (avoid sorted data worst case)
    const shuffled = this._shuffle([...animals]);

    let inserted = 0;
    let duplicates = 0;

    for (const animal of shuffled) {
      if (this.insert(animal)) {
        inserted++;
      } else {
        duplicates++;
      }
    }

    this.buildTime = Date.now() - startTime;

    return {
      size: this.size,
      inserted,
      duplicates,
      buildTime: this.buildTime,
      averageDepth: this._calculateAverageDepth(),
      maxDepth: this._calculateMaxDepth()
    };
  }

  /**
   * Fisher-Yates shuffle algorithm
   * @private
   */
  _shuffle(array) {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
  }

  /**
   * Calculate tree height (max depth)
   * @private
   */
  _calculateMaxDepth() {
    return this._maxDepthRecursive(this.root);
  }

  _maxDepthRecursive(node) {
    if (!node) return 0;
    
    const leftDepth = this._maxDepthRecursive(node.left);
    const rightDepth = this._maxDepthRecursive(node.right);
    
    return Math.max(leftDepth, rightDepth) + 1;
  }

  /**
   * Calculate average depth of all nodes
   * @private
   */
  _calculateAverageDepth() {
    if (!this.root) return 0;

    let totalDepth = 0;
    let nodeCount = 0;

    const traverse = (node, depth) => {
      if (!node) return;
      
      totalDepth += depth;
      nodeCount++;
      
      traverse(node.left, depth + 1);
      traverse(node.right, depth + 1);
    };

    traverse(this.root, 0);
    
    return nodeCount > 0 ? (totalDepth / nodeCount).toFixed(2) : 0;
  }

  /**
   * Get in-order traversal of all animals (sorted by animal_id)
   * @returns {Array} - Sorted array of animals
   */
  inOrderTraversal() {
    const result = [];
    
    const traverse = (node) => {
      if (!node) return;
      
      traverse(node.left);
      result.push(node.animal);
      traverse(node.right);
    };

    traverse(this.root);
    return result;
  }

  /**
   * Update an animal in the BST
   * @param {string} animalId - Animal ID to update
   * @param {Object} updatedAnimal - Updated animal data
   * @returns {boolean} - True if updated, false if not found
   */
  update(animalId, updatedAnimal) {
    const node = this._searchRecursive(this.root, animalId, { count: 0 });
    
    if (node) {
      node.animal = { ...node.animal, ...updatedAnimal };
      return true;
    }
    
    return false;
  }

  /**
   * Delete an animal from the BST
   * @param {string} animalId - Animal ID to delete
   * @returns {boolean} - True if deleted, false if not found
   * 
   */
  delete(animalId) {
    let deleted = false;
    this.root = this._deleteRecursive(this.root, animalId, deleted);
    
    if (deleted) {
      this.size--;
    }
    
    return deleted;
  }

  /**
   * Recursive helper for deletion
   * @private
   */
  _deleteRecursive(node, animalId, deletedRef) {
    if (!node) {
      return null;
    }

    if (animalId < node.animalId) {
      node.left = this._deleteRecursive(node.left, animalId, deletedRef);
      return node;
    } else if (animalId > node.animalId) {
      node.right = this._deleteRecursive(node.right, animalId, deletedRef);
      return node;
    }

    // Found the node to delete
    deletedRef = true;

    // Case 1: No children (leaf node)
    if (!node.left && !node.right) {
      return null;
    }

    // Case 2: One child
    if (!node.left) {
      return node.right;
    }
    if (!node.right) {
      return node.left;
    }

    // Case 3: Two children
    // Find minimum node in right subtree (in-order successor)
    let minRight = node.right;
    while (minRight.left) {
      minRight = minRight.left;
    }

    // Replace current node's data with successor's data
    node.animal = minRight.animal;
    node.animalId = minRight.animalId;

    // Delete the successor
    node.right = this._deleteRecursive(node.right, minRight.animalId, deletedRef);

    return node;
  }

  /**
   * Clear the entire tree
   */
  clear() {
    this.root = null;
    this.size = 0;
    this.buildTime = 0;
    this.totalComparisons = 0;
  }

  /**
   * Get tree statistics
   * @returns {Object} - Statistics about the BST
   */
  getStats() {
    return {
      size: this.size,
      maxDepth: this._calculateMaxDepth(),
      averageDepth: this._calculateAverageDepth(),
      buildTime: this.buildTime,
      totalComparisons: this.totalComparisons,
      isBalanced: this._isBalanced(),
      theoreticalOptimalDepth: Math.ceil(Math.log2(this.size + 1))
    };
  }

  /**
   * Check if tree is balanced (height difference <= 1 for all nodes)
   * @private
   */
  _isBalanced() {
    const checkBalance = (node) => {
      if (!node) return { balanced: true, height: 0 };

      const left = checkBalance(node.left);
      if (!left.balanced) return { balanced: false, height: 0 };

      const right = checkBalance(node.right);
      if (!right.balanced) return { balanced: false, height: 0 };

      const heightDiff = Math.abs(left.height - right.height);
      const balanced = heightDiff <= 1;
      const height = Math.max(left.height, right.height) + 1;

      return { balanced, height };
    };

    return checkBalance(this.root).balanced;
  }
}

module.exports = AnimalBST;
