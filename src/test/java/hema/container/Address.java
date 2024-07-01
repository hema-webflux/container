package hema.container;

import hema.container.annotation.Entity;

@Entity
record Address(int id, int userId, String city, String address) {
}