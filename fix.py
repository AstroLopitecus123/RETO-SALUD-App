import os

file_path = r"C:\Users\USER\AndroidStudioProjects\REPOSALUD\REPOSALUD v2.0\app\src\main\res\layout\activity_profile.xml"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

new_chunk = """            <!-- Menu Options -->
            <androidx.cardview.widget.CardView
                android:id="@+id/cvHistorial"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="32dp"
                android:clickable="true"
                android:focusable="true"
                android:background="?attr/selectableItemBackground"
                app:cardCornerRadius="16dp"
                app:cardElevation="2dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:padding="16dp">

                    <FrameLayout
                        android:layout_width="48dp"
                        android:layout_height="48dp"
                        android:background="@drawable/bg_category_icon">
                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:layout_gravity="center"
                            android:src="@drawable/ic_medical_cross"
                            app:tint="@color/primary_green" />
                    </FrameLayout>

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="16dp"
                        android:layout_weight="1"
                        android:orientation="vertical">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Historial Médico"
                            android:textColor="@color/dark_slate"
                            android:textSize="16sp"
                            android:textStyle="bold" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Última actualización: Ayer"
                            android:textColor="@color/light_text"
                            android:textSize="12sp" />
                    </LinearLayout>

                    <ImageView
                        android:layout_width="24dp"
                        android:layout_height="24dp"
                        android:src="@drawable/ic_arrow_forward"
                        app:tint="@color/light_text" />
                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <!-- Edit Profile -->
            <androidx.cardview.widget.CardView
                android:id="@+id/cvEditarPerfil"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:clickable="true"
                android:focusable="true"
                android:background="?attr/selectableItemBackground"
                app:cardCornerRadius="16dp"
                app:cardElevation="2dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:gravity="center_vertical"
                    android:padding="16dp">

                    <FrameLayout
                        android:layout_width="48dp"
                        android:layout_height="48dp"
                        android:background="@drawable/bg_category_icon">
                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:layout_gravity="center"
                            android:src="@drawable/ic_person"
                            app:tint="@color/primary_green" />
                    </FrameLayout>

                    <LinearLayout
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="16dp"
                        android:layout_weight="1"
                        android:orientation="vertical">
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Editar Perfil"
                            android:textColor="@color/dark_slate"
                            android:textSize="16sp"
                            android:textStyle="bold" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Actualiza tu información personal"
                            android:textColor="@color/light_text"
                            android:textSize="12sp" />
                    </LinearLayout>

                    <ImageView
                        android:layout_width="24dp"
                        android:layout_height="24dp"
                        android:src="@drawable/ic_arrow_forward"
                        app:tint="@color/light_text" />
                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:orientation="horizontal">

                <!-- Notifications Card -->
                <androidx.cardview.widget.CardView
                    android:id="@+id/cvNotificaciones"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginEnd="8dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:background="?attr/selectableItemBackground"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="2dp">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">
                        <FrameLayout
                            android:layout_width="40dp"
                            android:layout_height="40dp"
                            android:background="@drawable/bg_category_icon">
                            <ImageView
                                android:layout_width="20dp"
                                android:layout_height="20dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_shield"
                                app:tint="@color/primary_green" />
                        </FrameLayout>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:text="Notificaciones"
                            android:textColor="@color/dark_slate"
                            android:textStyle="bold" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Alertas, Sonidos"
                            android:textColor="@color/light_text"
                            android:textSize="12sp" />
                    </LinearLayout>
                </androidx.cardview.widget.CardView>

                <!-- Settings Card -->
                <androidx.cardview.widget.CardView
                    android:id="@+id/cvAjustes"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="8dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:background="?attr/selectableItemBackground"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="2dp">
                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:padding="16dp">
                        <FrameLayout
                            android:layout_width="40dp"
                            android:layout_height="40dp"
                            android:background="@drawable/bg_category_icon">
                            <ImageView
                                android:layout_width="20dp"
                                android:layout_height="20dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_person"
                                app:tint="@color/primary_green" />
                        </FrameLayout>
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="12dp"
                            android:text="Ajustes"
                            android:textColor="@color/dark_slate"
                            android:textStyle="bold" />
                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Idioma"
                            android:textColor="@color/light_text"
                            android:textSize="12sp" />
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
            </LinearLayout>

"""

idx_start = content.find("<!-- Menu Options -->")
idx_end = content.find("<!-- Logout Button -->")

if idx_start != -1 and idx_end != -1:
    new_content = content[:idx_start] + new_chunk + content[idx_end:]
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Fixed successfully!")
else:
    print("Could not find boundaries")
