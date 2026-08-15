package com.ascend.invest.admin.data.repository;

import com.google.firebase.database.DatabaseReference;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AdminRepository_Factory implements Factory<AdminRepository> {
  private final Provider<DatabaseReference> databaseReferenceProvider;

  public AdminRepository_Factory(Provider<DatabaseReference> databaseReferenceProvider) {
    this.databaseReferenceProvider = databaseReferenceProvider;
  }

  @Override
  public AdminRepository get() {
    return newInstance(databaseReferenceProvider.get());
  }

  public static AdminRepository_Factory create(
      Provider<DatabaseReference> databaseReferenceProvider) {
    return new AdminRepository_Factory(databaseReferenceProvider);
  }

  public static AdminRepository newInstance(DatabaseReference databaseReference) {
    return new AdminRepository(databaseReference);
  }
}
