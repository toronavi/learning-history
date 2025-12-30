# Dagger 2

Dagger 2は、JavaおよびAndroid向けの静的なコンパイル時依存関係注入（DI）フレームワークです。アノテーションプロセッサを利用して、コンパイル時にDIに関連するコードを自動生成するため、リフレクションを使わず高速に動作します。

Hiltは、このDagger 2をベースにしており、Android開発における定型的な実装をより簡潔に書けるようにしたライブラリです。Dagger 2の持つ強力な機能はそのままに、セットアップの複雑さを大幅に軽減しています。

## 1. セットアップ

`build.gradle` (モジュールレベル) に以下の依存関係を追加します。

```groovy
plugins {
    id 'kotlin-kapt'
}

android {
    // ...
}

dependencies {
    implementation 'com.google.dagger:dagger:2.48'
    kapt 'com.google.dagger:dagger-compiler:2.48'
}
```

## 2. 基本的な使い方

Dagger 2の基本的な構成要素は `@Inject`, `@Module`, `@Component` の3つです。

### a. `@Inject` による依存性の要求

クラスのコンストラクタに `@Inject` アノテーションを付けると、Daggerにそのクラスのインスタンス生成方法を教えることができます（コンストラクタインジェクション）。

```kotlin
class UserRepository @Inject constructor() {
    // ...
}

class UserViewModel @Inject constructor(private val repository: UserRepository) {
    // ...
}
```

### b. `@Module` による依存性の提供

コンストラクタインジェクションが使えない場合（インターフェースや外部ライブラリのクラスなど）は、`@Module`を使ってDaggerにインスタンスの提供方法を教えます。

- `@Module`: このクラスがDaggerモジュールであることを示します。
- `@Provides`: このメソッドが依存性を提供するものであることを示します。

```kotlin
// 例: Retrofitのような外部ライブラリのインスタンスを提供
@Module
class NetworkModule {

    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

### c. `@Component` による繋ぎ込み

`@Component` は、依存性を要求する側（例: Activity）と、依存性を提供する側（`@Module`など）を繋ぐ役割を持つインターフェースです。

- `modules`: このコンポーネントが利用するモジュールを指定します。
- `inject()` メソッド: 依存性を注入する対象のクラスを引数に取ります。

```kotlin
@Component(modules = [NetworkModule::class])
interface ApplicationComponent {
    fun inject(activity: MainActivity)
}
```

### d. コンポーネントの利用とインジェクションの実行

作成したコンポーネントは、`Dagger` + (コンポーネント名) という名前で自動生成されるクラスを使って初期化します。インジェクションを実行するには、対象のインスタンスを `inject()` メソッドに渡します。

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Daggerによってインスタンスが注入される
    @Inject
    lateinit var viewModel: UserViewModel
    
    @Inject
    lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        // DaggerApplicationComponentはDaggerが自動生成するクラス
        val appComponent: ApplicationComponent = DaggerApplicationComponent.create()
        appComponent.inject(this)

        super.onCreate(savedInstanceState)
        
        // この時点で viewModel と retrofit は初期化されている
    }
}
```

このように、Dagger 2はHiltに比べて手動でのセットアップやコンポーネントの管理が必要になりますが、その分柔軟な構成が可能です。
