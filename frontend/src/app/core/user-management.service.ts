import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { ApiConfiguration } from './api/api-configuration';
import { createUser } from './api/fn/user-controller/create-user';
import { deleteUser } from './api/fn/user-controller/delete-user';
import { listUsers } from './api/fn/user-controller/list-users';
import { updateUser } from './api/fn/user-controller/update-user';
import { CreateUserRequest } from './api/models/create-user-request';
import { UpdateUserRequest } from './api/models/update-user-request';

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfiguration);

  listUsers() {
    return listUsers(this.http, this.apiConfig.rootUrl, {
      pageable: { page: 0, size: 50, sort: ['username,asc'] }
    }).pipe(map((response) => response.body));
  }

  createUser(body: CreateUserRequest) {
    return createUser(this.http, this.apiConfig.rootUrl, { body }).pipe(
      map((response) => response.body)
    );
  }

  updateUser(id: number, body: UpdateUserRequest) {
    return updateUser(this.http, this.apiConfig.rootUrl, { id, body }).pipe(
      map((response) => response.body)
    );
  }

  deleteUser(id: number) {
    return deleteUser(this.http, this.apiConfig.rootUrl, { id }).pipe(map(() => undefined));
  }
}
