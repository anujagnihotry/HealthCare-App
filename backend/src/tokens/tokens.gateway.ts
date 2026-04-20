import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { TokensService } from './tokens.service';
import { Logger } from '@nestjs/common';

@WebSocketGateway({
  cors: { origin: '*' },
  namespace: '/tokens',
})
export class TokensGateway
  implements OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer()
  server: Server;

  private logger = new Logger('TokensGateway');

  constructor(private tokensService: TokensService) {}

  handleConnection(client: Socket) {
    this.logger.log(`Client connected: ${client.id}`);
  }

  handleDisconnect(client: Socket) {
    this.logger.log(`Client disconnected: ${client.id}`);
  }

  /**
   * Client joins a room for a specific doctor+location+date queue.
   * Room name: "queue:{doctorId}:{locationId}:{date}"
   */
  @SubscribeMessage('joinQueue')
  async handleJoinQueue(
    @ConnectedSocket() client: Socket,
    @MessageBody()
    data: { doctorId: string; locationId: string; date: string },
  ) {
    const room = `queue:${data.doctorId}:${data.locationId}:${data.date}`;
    await client.join(room);
    this.logger.log(`Client ${client.id} joined room ${room}`);

    // Send current state
    const currentToken = await this.tokensService.getCurrentToken(
      data.doctorId,
      data.locationId,
      data.date,
    );
    client.emit('tokenUpdate', currentToken);
  }

  @SubscribeMessage('leaveQueue')
  async handleLeaveQueue(
    @ConnectedSocket() client: Socket,
    @MessageBody()
    data: { doctorId: string; locationId: string; date: string },
  ) {
    const room = `queue:${data.doctorId}:${data.locationId}:${data.date}`;
    await client.leave(room);
  }

  /**
   * Called by the service layer to broadcast token updates to all
   * clients watching a specific queue.
   */
  emitTokenUpdate(
    doctorId: string,
    locationId: string,
    date: string,
    data: { currentToken: number | null; totalWaiting: number },
  ) {
    const room = `queue:${doctorId}:${locationId}:${date}`;
    this.server.to(room).emit('tokenUpdate', data);
  }
}
