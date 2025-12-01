const express = require('express');
const { Web3 } = require('web3');
require('dotenv').config();

const app = express();
const PORT = process.env.BLOCKCHAIN_SERVICE_PORT || 3003;

// Initialize Web3
const web3 = new Web3(process.env.BLOCKCHAIN_RPC_URL || 'http://localhost:8545');
let account = null;

try {
  if (process.env.BLOCKCHAIN_PRIVATE_KEY && process.env.BLOCKCHAIN_PRIVATE_KEY.startsWith('0x')) {
    account = web3.eth.accounts.privateKeyToAccount(process.env.BLOCKCHAIN_PRIVATE_KEY);
    web3.eth.accounts.wallet.add(account);
  }
} catch (err) {
  console.warn('⚠️  Blockchain: Private key not configured. Some features will be limited.');
}

// Middleware
app.use(express.json());

// Contract ABI (Reward Token)
const REWARD_CONTRACT_ABI = [
  {
    "inputs": [{"internalType": "address", "name": "to", "type": "address"}, {"internalType": "uint256", "name": "amount", "type": "uint256"}],
    "name": "transfer",
    "outputs": [{"internalType": "bool", "name": "", "type": "bool"}],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [{"internalType": "address", "name": "account", "type": "address"}],
    "name": "balanceOf",
    "outputs": [{"internalType": "uint256", "name": "", "type": "uint256"}],
    "stateMutability": "view",
    "type": "function"
  }
];

const NFT_CONTRACT_ABI = [
  {
    "inputs": [{"internalType": "address", "name": "to", "type": "address"}, {"internalType": "string", "name": "uri", "type": "string"}],
    "name": "mint",
    "outputs": [{"internalType": "uint256", "name": "", "type": "uint256"}],
    "stateMutability": "nonpayable",
    "type": "function"
  },
  {
    "inputs": [{"internalType": "address", "name": "owner", "type": "address"}],
    "name": "balanceOf",
    "outputs": [{"internalType": "uint256", "name": "", "type": "uint256"}],
    "stateMutability": "view",
    "type": "function"
  }
];

// Initialize contracts
const rewardContract = new web3.eth.Contract(
  REWARD_CONTRACT_ABI,
  process.env.BLOCKCHAIN_CONTRACT_ADDRESS
);

const nftContract = new web3.eth.Contract(
  NFT_CONTRACT_ABI,
  process.env.NFT_CONTRACT_ADDRESS
);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'Blockchain Service OK', network: process.env.BLOCKCHAIN_NETWORK });
});

// Get user reward balance
app.get('/blockchain/rewards/:userAddress', async (req, res) => {
  try {
    const { userAddress } = req.params;

    // Validate address
    if (!web3.utils.isAddress(userAddress)) {
      return res.status(400).json({ error: 'Invalid wallet address' });
    }

    // Get reward token balance
    const balance = await rewardContract.methods.balanceOf(userAddress).call();
    const balanceInEther = web3.utils.fromWei(balance, 'ether');

    // Get NFT count
    const nftBalance = await nftContract.methods.balanceOf(userAddress).call();

    res.json({
      userAddress,
      rewardBalance: balanceInEther,
      nftCount: parseInt(nftBalance),
      network: process.env.BLOCKCHAIN_NETWORK
    });
  } catch (error) {
    console.error('Error fetching rewards:', error);
    res.status(500).json({ error: 'Failed to fetch rewards', details: error.message });
  }
});

// Transfer rewards
app.post('/blockchain/transfer-rewards', async (req, res) => {
  try {
    const { toAddress, amount } = req.body;

    if (!web3.utils.isAddress(toAddress)) {
      return res.status(400).json({ error: 'Invalid recipient address' });
    }

    if (!amount || amount <= 0) {
      return res.status(400).json({ error: 'Invalid amount' });
    }

    const amountInWei = web3.utils.toWei(amount.toString(), 'ether');

    // Build transaction
    const tx = {
      from: account.address,
      to: rewardContract.options.address,
      data: rewardContract.methods.transfer(toAddress, amountInWei).encodeABI(),
      gas: 100000,
      gasPrice: await web3.eth.getGasPrice()
    };

    // Sign and send transaction
    const signedTx = await web3.eth.accounts.signTransaction(tx, process.env.BLOCKCHAIN_PRIVATE_KEY);
    const receipt = await web3.eth.sendSignedTransaction(signedTx.rawTransaction);

    res.json({
      success: true,
      transactionHash: receipt.transactionHash,
      blockNumber: receipt.blockNumber,
      to: toAddress,
      amount: amount
    });
  } catch (error) {
    console.error('Error transferring rewards:', error);
    res.status(500).json({ error: 'Failed to transfer rewards', details: error.message });
  }
});

// Mint NFT achievement
app.post('/blockchain/mint-nft', async (req, res) => {
  try {
    const { toAddress, achievementType, metadataURI } = req.body;

    if (!web3.utils.isAddress(toAddress)) {
      return res.status(400).json({ error: 'Invalid recipient address' });
    }

    if (!achievementType) {
      return res.status(400).json({ error: 'Achievement type required' });
    }

    const uri = metadataURI || `ipfs://achievement/${achievementType}`;

    // Build transaction
    const tx = {
      from: account.address,
      to: nftContract.options.address,
      data: nftContract.methods.mint(toAddress, uri).encodeABI(),
      gas: 300000,
      gasPrice: await web3.eth.getGasPrice()
    };

    // Sign and send transaction
    const signedTx = await web3.eth.accounts.signTransaction(tx, process.env.BLOCKCHAIN_PRIVATE_KEY);
    const receipt = await web3.eth.sendSignedTransaction(signedTx.rawTransaction);

    res.json({
      success: true,
      achievementType,
      recipientAddress: toAddress,
      transactionHash: receipt.transactionHash,
      blockNumber: receipt.blockNumber
    });
  } catch (error) {
    console.error('Error minting NFT:', error);
    res.status(500).json({ error: 'Failed to mint NFT', details: error.message });
  }
});

// Verify transaction
app.get('/blockchain/verify/:transactionHash', async (req, res) => {
  try {
    const { transactionHash } = req.params;

    const receipt = await web3.eth.getTransactionReceipt(transactionHash);

    if (!receipt) {
      return res.status(404).json({ error: 'Transaction not found' });
    }

    res.json({
      transactionHash,
      blockNumber: receipt.blockNumber,
      status: receipt.status ? 'confirmed' : 'failed',
      gasUsed: receipt.gasUsed,
      from: receipt.from,
      to: receipt.to,
      confirmations: (await web3.eth.getBlockNumber()) - receipt.blockNumber
    });
  } catch (error) {
    console.error('Error verifying transaction:', error);
    res.status(500).json({ error: 'Failed to verify transaction', details: error.message });
  }
});

// Get wallet info
app.get('/blockchain/wallet', (req, res) => {
  res.json({
    address: account ? account.address : 'Not configured',
    network: process.env.BLOCKCHAIN_NETWORK,
    rpcUrl: process.env.BLOCKCHAIN_RPC_URL,
    status: account ? 'ready' : 'unconfigured'
  });
});

app.listen(PORT, () => {
  console.log(`🚀 Blockchain Service running on http://localhost:${PORT}`);
  console.log(`📡 Network: ${process.env.BLOCKCHAIN_NETWORK}`);
  if (account) {
    console.log(`👛 Wallet: ${account.address}`);
  } else {
    console.log(`⚠️  Wallet: Not configured (set BLOCKCHAIN_PRIVATE_KEY in .env)`);
  }
});
