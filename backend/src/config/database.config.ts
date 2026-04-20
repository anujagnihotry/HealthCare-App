import { TypeOrmModuleOptions } from '@nestjs/typeorm';
import { ConfigService } from '@nestjs/config';

const entityGlob = __dirname + '/../**/*.entity{.ts,.js}';

function isProd(configService: ConfigService): boolean {
  return configService.get('NODE_ENV') === 'production';
}

export const getDatabaseConfig = (
  configService: ConfigService,
): TypeOrmModuleOptions => {
  const databaseUrl = configService.get<string>('DATABASE_URL');
  const syncExplicit = configService.get('TYPEORM_SYNC') === 'true';
  const synchronize =
    syncExplicit || configService.get('NODE_ENV') === 'development';

  const sslEnabled =
    configService.get('DB_SSL') === 'true' ||
    (isProd(configService) && !!databaseUrl);

  const ssl = sslEnabled ? { rejectUnauthorized: false } : false;

  if (databaseUrl) {
    return {
      type: 'postgres',
      url: databaseUrl,
      entities: [entityGlob],
      synchronize,
      logging: configService.get('NODE_ENV') === 'development',
      ssl,
    };
  }

  return {
    type: 'postgres',
    host: configService.get('DB_HOST', 'localhost'),
    port: configService.get<number>('DB_PORT', 5432),
    username: configService.get('DB_USERNAME', 'healthcare_user'),
    password: configService.get('DB_PASSWORD', 'healthcare_pass_2024'),
    database: configService.get('DB_NAME', 'healthcare_db'),
    entities: [entityGlob],
    synchronize,
    logging: configService.get('NODE_ENV') === 'development',
    ssl,
  };
};
