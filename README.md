# Minecraft Server Query

## 构建

### 构建Debug版本

```bash
./gradlew assembleDebug
```

### 构建Release版本

```bash
./gradlew assembleRelease
```

### 预生成密钥库

```bash
./gradlew createKeystore
```

## APK输出位置

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

构建完成后，Release APK会自动签名并输出到 `app/build/outputs/apk/release/app-release.apk`

## 密钥配置

### 环境变量配置

你可以通过以下环境变量自定义密钥配置：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `KEYSTORE_PATH` | 密钥库文件路径 | `keystore/app.jks` |
| `KEYSTORE_PASSWORD` | 密钥库密码 | `android123` |
| `KEY_PASSWORD` | 密钥密码 | `android123` |
| `KEY_ALIAS` | 密钥别名 | `myapp-key` |

### 使用示例

```bash
# 使用自定义密钥配置构建
KEYSTORE_PATH=/path/to/custom.jks \
KEYSTORE_PASSWORD=your_password \
KEY_PASSWORD=your_key_password \
KEY_ALIAS=your_key_alias \
./gradlew assembleRelease

# 或者在 Windows 上
set KEYSTORE_PATH=keystore\custom.jks
set KEYSTORE_PASSWORD=your_password
set KEY_PASSWORD=your_key_password
set KEY_ALIAS=your_key_alias
gradlew.bat assembleRelease
```

### 密钥库生成

运行 `createKeystore` 任务会自动生成密钥库文件：

```bash
./gradlew createKeystore
```

该任务会：
- 在 `keystore/` 目录下创建 `app.jks` 文件
- 使用 RSA 2048 位密钥对
- 有效期 10000 天
- 默认密码：`android123`

### 密钥库信息

生成的密钥库包含：
- **存储类型**: JKS
- **密钥算法**: RSA
- **密钥长度**: 2048 位
- **有效期**: 10000 天
- **别名**: `myapp-key` (可通过 `KEY_ALIAS` 环境变量修改)

> ⚠️ **安全提示**: 请妥善保管密钥库文件和密码。建议使用 Git 忽略 `keystore/` 目录。

## 项目配置

- **编译 SDK**: 34
- **最小 SDK**: 28
- **目标 SDK**: 34
- **语言**: Java 17
