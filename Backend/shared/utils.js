// Shared utility functions
const generateToken = (payload) => {
  const jwt = require('jsonwebtoken');
  return jwt.sign(payload, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRATION || '7d'
  });
};

const verifyToken = (token) => {
  const jwt = require('jsonwebtoken');
  try {
    return jwt.verify(token, process.env.JWT_SECRET);
  } catch (err) {
    return null;
  }
};

const hashPassword = async (password) => {
  const bcrypt = require('bcryptjs');
  return bcrypt.hash(password, 10);
};

const comparePasswords = async (password, hash) => {
  const bcrypt = require('bcryptjs');
  return bcrypt.compare(password, hash);
};

module.exports = {
  generateToken,
  verifyToken,
  hashPassword,
  comparePasswords
};
