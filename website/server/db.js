const knex = require('knex');
const path = require('path');

const db = knex({
  client: 'sqlite3',
  connection: {
    filename: path.join(__dirname, 'database.sqlite')
  },
  useNullAsDefault: true
});

async function initDb() {
  const hasUsers = await db.schema.hasTable('users');
  if (!hasUsers) {
    await db.schema.createTable('users', table => {
      table.increments('id').primary();
      table.string('uid').unique();
      table.string('email').unique();
      table.string('password_hash');
      table.string('full_name');
      table.string('username').unique();
      table.decimal('wallet_balance', 15, 2).defaultTo(0);
      table.decimal('unlocked_balance', 15, 2).defaultTo(0);
      table.decimal('locked_balance', 15, 2).defaultTo(0);
      table.decimal('total_deposit', 15, 2).defaultTo(0);
      table.decimal('total_profit', 15, 2).defaultTo(0);
      table.string('referral_code').unique();
      table.string('referred_by');
      table.string('status').defaultTo('offline');
      table.timestamp('last_seen').defaultTo(db.fn.now());
      table.timestamps(true, true);
    });
  }

  const hasTransactions = await db.schema.hasTable('transactions');
  if (!hasTransactions) {
    await db.schema.createTable('transactions', table => {
      table.increments('id').primary();
      table.string('transaction_id');
      table.integer('user_id').unsigned().references('id').inTable('users');
      table.string('title');
      table.string('subtitle');
      table.string('type');
      table.string('amount');
      table.string('status').defaultTo('Pending');
      table.string('user_wallet_address');
      table.string('transaction_hash');
      table.bigInteger('timestamp');
      table.timestamps(true, true);
    });
  } else {
    // Check if subtitle column exists (for existing databases)
    const hasTitle = await db.schema.hasColumn('transactions', 'title');
    if (!hasTitle) {
      await db.schema.table('transactions', table => {
        table.string('title');
      });
    }
    const hasSubtitle = await db.schema.hasColumn('transactions', 'subtitle');
    if (!hasSubtitle) {
      await db.schema.table('transactions', table => {
        table.string('subtitle');
      });
    }
    const hasTxHash = await db.schema.hasColumn('transactions', 'transaction_hash');
    if (!hasTxHash) {
      await db.schema.table('transactions', table => {
        table.string('transaction_hash');
      });
    }
    const hasWalletAddr = await db.schema.hasColumn('transactions', 'user_wallet_address');
    if (!hasWalletAddr) {
      await db.schema.table('transactions', table => {
        table.string('user_wallet_address');
      });
    }
    const hasTimestamp = await db.schema.hasColumn('transactions', 'timestamp');
    if (!hasTimestamp) {
      await db.schema.table('transactions', table => {
        table.bigInteger('timestamp');
      });
    }
    const hasTxId = await db.schema.hasColumn('transactions', 'transaction_id');
    if (!hasTxId) {
      await db.schema.table('transactions', table => {
        table.string('transaction_id');
      });
    }
    const hasType = await db.schema.hasColumn('transactions', 'type');
    if (!hasType) {
      await db.schema.table('transactions', table => {
        table.string('type');
      });
    }
    const hasAmount = await db.schema.hasColumn('transactions', 'amount');
    if (!hasAmount) {
      await db.schema.table('transactions', table => {
        table.string('amount');
      });
    }
    const hasStatus = await db.schema.hasColumn('transactions', 'status');
    if (!hasStatus) {
      await db.schema.table('transactions', table => {
        table.string('status').defaultTo('Pending');
      });
    }
    const hasCrypto = await db.schema.hasColumn('transactions', 'crypto');
    if (!hasCrypto) {
      await db.schema.table('transactions', table => {
        table.string('crypto');
      });
    }
    const hasProtocolData = await db.schema.hasColumn('transactions', 'protocol_data');
    if (!hasProtocolData) {
      await db.schema.table('transactions', table => {
        table.text('protocol_data');
      });
    }
    const hasValidatorNode = await db.schema.hasColumn('transactions', 'validator_node');
    if (!hasValidatorNode) {
      await db.schema.table('transactions', table => {
        table.string('validator_node');
      });
    }
  }

  const hasOngoing = await db.schema.hasTable('ongoing_deposits_list');
  if (!hasOngoing) {
    await db.schema.createTable('ongoing_deposits_list', table => {
      table.increments('id').primary();
      table.string('transaction_id');
      table.integer('user_id').unsigned().references('id').inTable('users');
      table.string('amount');
      table.string('tx_hash');
      table.string('user_wallet_address');
      table.string('priority');
      table.string('crypto');
      table.string('validator_node');
      table.text('protocol_signature');
      table.timestamp('created_at').defaultTo(db.fn.now());
    });
  } else {
    const hasPriority = await db.schema.hasColumn('ongoing_deposits_list', 'priority');
    if (!hasPriority) {
      await db.schema.table('ongoing_deposits_list', table => {
        table.string('priority');
      });
    }
    const hasCrypto = await db.schema.hasColumn('ongoing_deposits_list', 'crypto');
    if (!hasCrypto) {
      await db.schema.table('ongoing_deposits_list', table => {
        table.string('crypto');
      });
    }
    const hasProtocolSig = await db.schema.hasColumn('ongoing_deposits_list', 'protocol_signature');
    if (!hasProtocolSig) {
      await db.schema.table('ongoing_deposits_list', table => {
        table.text('protocol_signature');
      });
    }
    const hasValidatorNode = await db.schema.hasColumn('ongoing_deposits_list', 'validator_node');
    if (!hasValidatorNode) {
      await db.schema.table('ongoing_deposits_list', table => {
        table.string('validator_node');
      });
    }
  }

  const hasOngoingWithdraw = await db.schema.hasTable('ongoing_withdraw_list');
  if (!hasOngoingWithdraw) {
    await db.schema.createTable('ongoing_withdraw_list', table => {
      table.increments('id').primary();
      table.string('transaction_id');
      table.integer('user_id').unsigned().references('id').inTable('users');
      table.string('amount');
      table.string('user_wallet_address');
      table.timestamp('created_at').defaultTo(db.fn.now());
    });
  } else {
    const hasCrypto = await db.schema.hasColumn('ongoing_withdraw_list', 'crypto');
    if (!hasCrypto) {
      await db.schema.table('ongoing_withdraw_list', table => {
        table.string('crypto');
      });
    }
  }

  const hasPlans = await db.schema.hasTable('plans');
  if (!hasPlans) {
    await db.schema.createTable('plans', table => {
      table.increments('id').primary();
      table.string('name');
      table.decimal('min_amount', 15, 2);
      table.decimal('max_amount', 15, 2);
      table.integer('duration_days');
      table.decimal('daily_interest', 5, 2);
      table.boolean('is_active').defaultTo(true);
    });
  }

  const hasSettings = await db.schema.hasTable('settings');
  if (!hasSettings) {
    await db.schema.createTable('settings', table => {
      table.string('key').primary();
      table.string('value');
    });
    await db('settings').insert([
      { key: 'deposit_address', value: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e' },
      { key: 'deposit_address_usdt', value: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e' },
      { key: 'deposit_address_btc', value: '1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa' },
      { key: 'deposit_address_eth', value: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e' },
      { key: 'deposit_address_bnb', value: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e' }
    ]);
  }
}

module.exports = { db, initDb };
