package com.ascend.invest.admin.di;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AdminAppModule_ProvideDatabaseReferenceFactory implements Factory<DatabaseReference> {
  private final Provider<FirebaseDatabase> databaseProvider;

  public AdminAppModule_ProvideDatabaseReferenceFactory(
      Provider<FirebaseDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DatabaseReference get() {
    return provideDatabaseReference(databaseProvider.get());
  }

  public static AdminAppModule_ProvideDatabaseReferenceFactory create(
      Provider<FirebaseDatabase> databaseProvider) {
    return new AdminAppModule_ProvideDatabaseReferenceFactory(databaseProvider);
  }

  public static DatabaseReference provideDatabaseReference(FirebaseDatabase database) {
    return Preconditions.checkNotNullFromProvides(AdminAppModule.provideDatabaseReference(database));
  }
}
