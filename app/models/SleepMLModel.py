import pandas as pd
import joblib

from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import train_test_split
import numpy as np
from sklearn.metrics import (
    mean_absolute_error,
    r2_score,
    explained_variance_score,
    mean_squared_error,
)

# Load dataset
data = pd.read_csv(
    "C:\\Users\\idhan\\cs125project\\Quick-Health-Lifestyle\\app\\data\\sleep_dataset.csv"
)

X = data[["sleepTime", "id"]]
y = data["wakeupTime"]

# Split data into train and test sets
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

gbm = GradientBoostingRegressor(
    n_estimators=100, learning_rate=0.1, max_depth=3, random_state=1234
)
gbm.fit(X_train, y_train)

y_pred = gbm.predict(X_test)

# Evaluate the model
mse = mean_squared_error(y_test, y_pred)
print("Mean Squared Error:", mse)

rmse = np.sqrt(mse)
print("Root Mean Squared Error:", rmse)

# Mean Absolute Error (MAE)
mae = mean_absolute_error(y_test, y_pred)
print("Mean Absolute Error:", mae)

# R-squared (R^2) Score
r2 = r2_score(y_test, y_pred)
print("R-squared (R^2) Score:", r2)

# Explained Variance Score
explained_variance = explained_variance_score(y_test, y_pred)
print("Explained Variance Score:", explained_variance)

# Save the trained model
joblib.dump(gbm, "sleep_wakeup_gbm_model.pkl")
