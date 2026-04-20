import { DataSource } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { User } from '../../users/entities/user.entity';
import { UserRole } from '../../common/enums';

async function seedAdmin() {
  const dataSource = new DataSource({
    type: 'postgres',
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT || '5432'),
    username: process.env.DB_USERNAME || 'healthcare_user',
    password: process.env.DB_PASSWORD || 'healthcare_pass_2024',
    database: process.env.DB_NAME || 'healthcare_db',
    entities: [__dirname + '/../../**/*.entity{.ts,.js}'],
    synchronize: true,
  });

  await dataSource.initialize();
  console.log('Database connected...');

  const email = process.env.SUPER_ADMIN_EMAIL || 'admin@healthcare.com';
  const password = process.env.SUPER_ADMIN_PASSWORD || 'Admin@123';

  const userRepo = dataSource.getRepository(User);

  const existing = await userRepo.findOne({ where: { email } });
  if (existing) {
    if (existing.role !== UserRole.SUPER_ADMIN) {
      await userRepo.update(existing.id, { role: UserRole.SUPER_ADMIN });
      console.log(`Updated existing user ${email} to SUPER_ADMIN role.`);
    } else {
      console.log(`Super admin ${email} already exists. Skipping.`);
    }
    await dataSource.destroy();
    return;
  }

  const passwordHash = await bcrypt.hash(password, 12);
  const admin = userRepo.create({
    email,
    passwordHash,
    role: UserRole.SUPER_ADMIN,
    isActive: true,
  });
  await userRepo.save(admin);

  console.log('');
  console.log('Super admin created successfully!');
  console.log(`  Email:    ${email}`);
  console.log(`  Password: ${password}`);
  console.log('');
  console.log('Login at: POST /api/admin/auth/login');

  await dataSource.destroy();
}

seedAdmin().catch((err) => {
  console.error('Admin seed failed:', err);
  process.exit(1);
});
