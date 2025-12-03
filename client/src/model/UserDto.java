package model;

/**
 * 对应后端 ListUsers / CreateUser 返回的用户信息。
 */
public class UserDto {
    public int id;
    public String username;
    public String role;   // "admin" or "user"
}
