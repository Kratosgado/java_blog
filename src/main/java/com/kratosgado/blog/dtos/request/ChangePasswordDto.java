
package com.kratosgado.blog.dtos.request;

public record ChangePasswordDto(int id, String oldPassword, String newPassword, String confirmNewPassword) {
}
